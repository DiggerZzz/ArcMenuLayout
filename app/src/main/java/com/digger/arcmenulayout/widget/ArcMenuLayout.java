package com.digger.arcmenulayout.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;

import com.digger.arcmenulayout.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author: DiggerZzz
 * Date: 2019/12/30 16:25
 * Desc:
 */
public class ArcMenuLayout extends ViewGroup {

    private final int DEFAULT_RADIUS = 144;

    //弧形菜单方向
    private int mDirection;
    //弧形菜单半径
    private int mGroupRadius;
    //弧形菜单item距轴中心半径
    private int mAxisRadius;
    //弧形菜单item间隔角度
    private float mSpaceAngle;
    //弧形菜单背景
    private Drawable mBackground;

    private final RectF srcRect = new RectF();

    private Path mPath = new Path();
    private Paint mPaint = new Paint();
    private Xfermode xfermode;

    public ArcMenuLayout(Context context) {
        this(context, null);
    }

    public ArcMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defWidth = 0, defHeight = 0;

        switch(mDirection) {
            case Direction.UP:
            case Direction.DOWN:
                defWidth = mGroupRadius * 2;
                defHeight = mGroupRadius;
                break;
            case Direction.LEFT:
            case Direction.RIGHT:
                defWidth = mGroupRadius;
                defHeight = mGroupRadius * 2;
                break;
        }

        setMeasuredDimension(
                computeMeasureSize(widthMeasureSpec, defWidth),
                computeMeasureSize(heightMeasureSpec, defHeight)
        );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        srcRect.set(0, 0, w, h);

        mPath.reset();
        switch(mDirection) {
            case Direction.UP:
                mPath.addCircle((int) (srcRect.width() / 2), (int) srcRect.height(), mGroupRadius, Path.Direction.CCW);
                break;
            case Direction.DOWN:
                mPath.addCircle((int) (srcRect.width() / 2), 0, mGroupRadius, Path.Direction.CCW);
                break;
            case Direction.LEFT:
                mPath.addCircle((int) srcRect.width(), (int) (srcRect.height() / 2), mGroupRadius, Path.Direction.CCW);
                break;
            case Direction.RIGHT:
                mPath.addCircle(0, (int) (srcRect.height() / 2), mGroupRadius, Path.Direction.CCW);
                break;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        switch(mDirection) {
            case Direction.UP:
                layoutUpDirection();
                break;
            case Direction.DOWN:
                layoutDownDirection();
                break;
            case Direction.LEFT:
                layoutLeftDirection();
                break;
            case Direction.RIGHT:
                layoutRightDirection();
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mBackground == null || isInEditMode())
            return;

        canvas.saveLayer(srcRect, null, Canvas.ALL_SAVE_FLAG);

        //绘制背景
        mBackground.setBounds((int) srcRect.left, (int) srcRect.top, (int) srcRect.right, (int) srcRect.bottom);
        mBackground.draw(canvas);

        //绘制扇形显示区域路径
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            mPaint.setXfermode(xfermode);
            canvas.drawPath(mPath, mPaint);
        } else {
            mPaint.setXfermode(xfermode);

            final Path p = new Path();
            p.addRect(0, 0, srcRect.width(), srcRect.height(), Path.Direction.CCW);
            p.op(mPath, Path.Op.DIFFERENCE);
            canvas.drawPath(p, mPaint);
        }

        canvas.restore();
    }

    ///////////////////////////////////////////////////////////////////////////
    // private method
    ///////////////////////////////////////////////////////////////////////////
    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(false);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ArcMenuLayout);

        int direction = typedArray.getInt(
                R.styleable.ArcMenuLayout_arc_group_direction, Direction.UP
        );
        int groupRadius = typedArray.getDimensionPixelSize(
                R.styleable.ArcMenuLayout_arc_group_radius, DEFAULT_RADIUS
        );
        int axisRadius = typedArray.getDimensionPixelSize(
                R.styleable.ArcMenuLayout_arc_group_axis_radius, DEFAULT_RADIUS
        );
        float spaceAngle = typedArray.getFloat(
                R.styleable.ArcMenuLayout_arc_group_space_angle, 30f
        );
        Drawable background = typedArray.getDrawable(
                R.styleable.ArcMenuLayout_arc_group_background
        );

        typedArray.recycle();

        setDirection(direction);
        setGroupRadius(groupRadius);
        setAxisRadiusRadius(axisRadius);
        setSpaceAngle(spaceAngle);
        setBackground(background);

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        } else {
            xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
        }
    }

    private int computeMeasureSize(int measureSpec, int defSize) {
        final int mode = MeasureSpec.getMode(measureSpec);

        switch(mode) {
            case MeasureSpec.EXACTLY:
                return MeasureSpec.getSize(measureSpec);
            default:
                return defSize;
        }
    }

    private int x(int radius, float degrees) {
        return Math.round(computeCircleX(radius, degrees));
    }

    private int y(int radius, float degrees) {
        return Math.round(computeCircleY(radius, degrees));
    }

    private float computeCircleX(float r, float degrees) {
        return (float) (r * Math.cos(Math.toRadians(degrees)));
    }

    private float computeCircleY(float r, float degrees) {
        return (float) (r * Math.sin(Math.toRadians(degrees)));
    }

    private void measureChild(View child) {
        final LayoutParams lp = child.getLayoutParams();

        int widthSize;

        switch(lp.width) {
            case LayoutParams.MATCH_PARENT:
            case LayoutParams.WRAP_CONTENT:
                widthSize = (int) Math.max(srcRect.width(), srcRect.height()) / getChildCount();
                break;
            default:
                widthSize = lp.width;
                break;
        }

        int heightSize;

        switch(lp.height) {
            case LayoutParams.MATCH_PARENT:
            case LayoutParams.WRAP_CONTENT:
                heightSize = (int) Math.max(srcRect.width(), srcRect.height()) / getChildCount();
                break;
            default:
                heightSize = lp.height;
                break;
        }

        child.measure(
                MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)
        );
    }

    private void layoutUpDirection() {
        float sweepAngle = (getChildCount() - 1) * mSpaceAngle;
        float startAngleExtra = (180f - sweepAngle) / 2f;

        for(int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            float childAngle = startAngleExtra + i * mSpaceAngle;
            int layoutX = -x(mAxisRadius, childAngle) + mGroupRadius;
            int layoutY = (int) (srcRect.height() - y(mAxisRadius, childAngle));

            measureChild(child);

            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();

            int left = layoutX - (width / 2);
            int top = layoutY - (height / 2);

            child.layout(left, top, left + width, top + height);
        }
    }

    private void layoutDownDirection() {
        float sweepAngle = (getChildCount() - 1) * mSpaceAngle;
        float startAngleExtra = (180f - sweepAngle) / 2f;

        for(int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            float childAngle = startAngleExtra + i * mSpaceAngle;
            int layoutX = x(mAxisRadius, childAngle) + mGroupRadius;
            int layoutY = y(mAxisRadius, childAngle);

            measureChild(child);

            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();

            int left = layoutX - (width / 2);
            int top = layoutY - (height / 2);

            child.layout(left, top, left + width, top + height);
        }
    }

    private void layoutLeftDirection() {
        float sweepAngle = (getChildCount() - 1) * mSpaceAngle;
        float startAngleExtra = (180f - sweepAngle) / 2f;

        for(int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            float childAngle = startAngleExtra + i * mSpaceAngle;
            int layoutX = (int) (srcRect.width() - y(mAxisRadius, childAngle));
            int layoutY = x(mAxisRadius, childAngle) + mGroupRadius;

            measureChild(child);

            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();

            int left = layoutX - (width / 2);
            int top = layoutY - (height / 2);

            child.layout(left, top, left + width, top + height);
        }
    }

    private void layoutRightDirection() {
        float sweepAngle = (getChildCount() - 1) * mSpaceAngle;
        float startAngleExtra = (180f - sweepAngle) / 2f;

        for(int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            float childAngle = startAngleExtra + i * mSpaceAngle;
            int layoutX = y(mAxisRadius, childAngle);
            int layoutY = -x(mAxisRadius, childAngle) + mGroupRadius;

            measureChild(child);

            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();

            int left = layoutX - (width / 2);
            int top = layoutY - (height / 2);

            child.layout(left, top, left + width, top + height);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // public method
    ///////////////////////////////////////////////////////////////////////////
    /**
     * 设置弧形菜单方向
     * @param direction
     */
    public void setDirection(@Direction int direction) {
        mDirection = direction;
    }

    /**
     * 设置弧形菜单半径
     * @param radius
     */
    public void setGroupRadius(int radius) {
        mGroupRadius = radius;
    }

    /**
     * 设置弧形菜单item距轴中心半径
     * @param radius
     */
    public void setAxisRadiusRadius(int radius) {
        mAxisRadius = radius;
    }

    /**
     * 设置弧形菜单item间隔角度
     * @param angle
     */
    public void setSpaceAngle(float angle) {
        mSpaceAngle = angle;
    }

    /**
     * 设置背景
     * @param d
     */
    public void setBackground(Drawable d) {
        if(mBackground == d)  {
            return;
        }

        mBackground = d;
        invalidate();
    }

    /**
     * 设置背景
     * @param drawableRes
     */
    public void setBackgroundResource(@DrawableRes int drawableRes) {
        if(drawableRes != 0) {
            setBackground(getContext().getResources().getDrawable(drawableRes));
        }
    }

    /**
     * 设置背景
     * @param color
     */
    public void setBackgroundColor(@ColorInt int color) {
        if(mBackground instanceof ColorDrawable) {
            ((ColorDrawable) mBackground.mutate()).setColor(color);
        } else {
            setBackground(new ColorDrawable(color));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // direction enum
    ///////////////////////////////////////////////////////////////////////////
    @IntDef({Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Direction {
        int UP = 0;
        int DOWN = 1;
        int LEFT = 2;
        int RIGHT = 3;
    }
}
