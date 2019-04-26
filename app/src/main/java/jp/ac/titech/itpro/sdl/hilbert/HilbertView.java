package jp.ac.titech.itpro.sdl.hilbert;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class HilbertView extends View {

    private Paint paint = new Paint();

    private Canvas canvas;

    private int order = 1;

    // for parallel drawing
    // --------------------
    // used for background drawing. Will be copied to the main view when drawing on this is done.
    private Bitmap bitmap;
    // canvas for the bitmap
    private Canvas bitmap_canvas;
    // parallel drawer: defined below
    private ParallelDrawer parallel_drawer;
    // thread for drawing
    private Thread parallel_drawer_thread;

    class ParallelDrawer implements Turtle.Drawer, Runnable {
        private List<int[]> draw_queue = new ArrayList<>();
        public void drawLine(double x0, double y0, double x1, double y1){
            draw_queue.add(new int[]{(int) x0, (int) y0, (int) x1, (int) y1});
        }

        public void run(){
            for(int[] i : draw_queue){
                bitmap_canvas.drawLine(i[0], i[1], i[2], i[3], paint);
            }
            postInvalidate();
        }
    }

    private HilbertTurtle turtle = new HilbertTurtle(new Turtle.Drawer() {
        @Override
        public void drawLine(double x0, double y0, double x1, double y1) {
            parallel_drawer.drawLine(x0, y0, x1, y1);
        }
    });

    public HilbertView(Context context) {
        this(context, null);
    }

    public HilbertView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HilbertView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;

        final int w = getWidth();
        final int h = getHeight();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // prepare the bit map for background drawing
                bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                bitmap_canvas = new Canvas(bitmap);

                paint.setColor(Color.DKGRAY);
                bitmap_canvas.drawRect(0, 0, w, h, paint);

                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(3);
                int size = Math.min(w, h);
                double step = (double) size / (1 << order);
                turtle.setPos((w - size + step) / 2, (h + size - step) / 2);
                turtle.setDir(HilbertTurtle.E);
                turtle.draw(order, step, HilbertTurtle.R);
                parallel_drawer_thread.start();
            }
        }).start();

        if(bitmap != null)
        canvas.drawBitmap(bitmap, 0, 100, null);
    }

    public void setOrder(int n) {
        order = n;
        parallel_drawer = new ParallelDrawer();
        parallel_drawer_thread = new Thread(parallel_drawer);
    }
}
