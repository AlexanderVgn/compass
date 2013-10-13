package com.paad.compass;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View {
    private static final int DEFAULT_VIEW_SIZE = 200;

    private static final float ROTATE_ANGLE = 15;

    private static final float TEXT_SIZE = 25;

    private static final int LINE_LENGHT = 20;

    private static final int DOUBLE_LINE_LENGHT = 30;

    private float bearing;

    private Paint markerPaint;

    private Paint textPaint;

    private Paint circlePaint;

    private String northString;

    private String eastString;

    private String southString;

    private String westString;

    private float pitch = 0;

    private float roll = 0;

    private int textHeight;

    private int measureWidth;

    private int measureHeight;

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getBearing() {
        return bearing;
    }

    public CompassView(Context context) {
        super(context);
        initCompassView();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCompassView();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initCompassView();
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    protected void initCompassView() {
        setFocusable(true);

        Resources r = this.getResources();

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(r.getColor(R.color.background_color));
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        northString = r.getString(R.string.cardinal_north);
        eastString  = r.getString(R.string.cardinal_east);
        southString = r.getString(R.string.cardinal_south);
        westString  = r.getString(R.string.cardinal_west);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(r.getColor(R.color.text_color));
        textPaint.setTextSize(TEXT_SIZE);

        textHeight = (int) textPaint.measureText("yY");

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(r.getColor(R.color.marker_color));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Компас представляет собой окружность, занимающую все доступное пространство
        // Установите размеры элемента, вычислив короткую грань ( высоту или ширину )
        int measuredWidth =  measure(widthMeasureSpec);
        int measuredHeight = measure(heightMeasureSpec);

        int d = Math.min(measuredWidth, measuredHeight);
        measureWidth = d;
        measureHeight = d;
        setMeasuredDimension(d, d);
    }

    private int measure(int measureSpec) {
        int result = 0;

        // Декодируйте параметр measureSpec
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            // Если размеры не указанны, верните размер по умолчанию (200)
            result = DEFAULT_VIEW_SIZE;
        } else {
            result = specSize;
        }

        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int px = getMeasuredWidth() / 2;
        int py = getMeasuredHeight() / 2;

        int radius = Math.min(px,py);

        // Нарисуйте фон
        canvas.drawCircle(px, py, radius, circlePaint);

        // Поворачивайте ракурс таким образом, чтобы
        // "верх" всегда указывал на текущее направление
        canvas.save();
        canvas.rotate(-bearing, px, py);

        int textWidth = (int)textPaint.measureText("W");
        int cardinalX = (int)(px - textWidth / 2);
        int cardinalY = py - radius + textHeight;

        // Рисуйте отметки каждые 15' и текст каждые 45'
        for (int i = 0; i < 24; i++) {
            // Нарисуйте метку
            int doubleLineLen = LINE_LENGHT;
            if (i % 3 == 0) {
                doubleLineLen = DOUBLE_LINE_LENGHT;
            }
            canvas.drawLine(px, py - radius, px, py - radius + doubleLineLen, markerPaint );

            canvas.save();
            canvas.translate(0, textHeight);

            // Нарисуйте основные точки
            if (i % 6 == 0) {
                String dirString = "";
                switch (i) {
                    case (0): {
                                dirString = northString;
                                int arrowY = 2 * textHeight;
                                canvas.drawLine(px, arrowY, px - 5, 3 * textHeight, markerPaint );
                                canvas.drawLine(px, arrowY, px + 5, 3 * textHeight, markerPaint );
                                break;
                              }
                    case (6): dirString = eastString; break;
                    case (12): dirString = southString; break;
                    case (18): dirString = westString; break;
                }
                canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
            }
            else if (i % 3 == 0) {
                // Отображайте текст каждые 45'
                String angle = String.valueOf(i*15);
                float angleTextWidth = textPaint.measureText(angle);

                int angleTextX = (int)(px - angleTextWidth/2);
                int angleTextY = py - radius + textHeight;
                canvas.drawText(angle, angleTextX, angleTextY, textPaint);
            }
            canvas.restore();
            canvas.rotate(ROTATE_ANGLE, px, py);
        }
        canvas.restore();

        // Рисуем окружность автогоризонта (бокового наклона)
        RectF rollOval = new RectF( (measureWidth /3) - measureWidth /7,
                                    (measureHeight /2)- measureWidth /7,
                                    (measureWidth /3) + measureWidth /7,
                                    (measureHeight /2)+ measureWidth /7);
        markerPaint.setStyle(Paint.Style.STROKE);
        canvas.drawOval(rollOval, markerPaint);
        markerPaint.setStyle(Paint.Style.FILL);
        canvas.save();
        canvas.rotate(roll, measureWidth/3, measureHeight/2);
        canvas.drawArc(rollOval, 0, 180, false, markerPaint);

        canvas.restore();

        // Рисуем окружность автогоризонта (горизонтального наклона)
        RectF pitchOval = new RectF( (2*measureWidth /3) - measureWidth /7,
                                     (measureHeight /2)- measureWidth /7,
                                     (2*measureWidth /3) + measureWidth /7,
                                     (measureHeight /2)+ measureWidth /7);
        markerPaint.setStyle(Paint.Style.STROKE);
        canvas.drawOval(pitchOval, markerPaint);
        markerPaint.setStyle(Paint.Style.FILL);
        canvas.save();
        canvas.drawArc(pitchOval, 0 - pitch/2, 180 + (pitch), false, markerPaint);
        markerPaint.setStyle(Paint.Style.STROKE);
    }
}