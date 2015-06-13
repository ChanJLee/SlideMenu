package com.singleslidemenudemo;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;


public class MainActivity
        extends Activity
        implements View.OnTouchListener{

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 滚动显示和隐藏menu时，手指滑动需要达到的速度。 
     */
    public static final int SNAP_VELOCITY = 200;

    /**
     * 屏幕宽度值。 
     */
    private int m_screenWidth;

    /**
     * menu最多可以滑动到的左边缘。值由menu布局的宽度来定，marginLeft到达此值之后，不能再减少。 
     */
    private int m_leftEdge;

    /**
     * menu最多可以滑动到的右边缘。值恒为0，即marginLeft到达0之后，不能增加。 
     */
    private int m_rightEdge = 0;

    /**
     * menu完全显示时，留给content的宽度值。 
     */
    private int m_menuPadding = 80;

    /**
     * 主内容的布局。 
     */
    private View m_content;

    /**
     * menu的布局。 
     */
    private View m_menu;

    /**
     * menu布局的参数，通过此参数来更改leftMargin的值。 
     */
    private LinearLayout.LayoutParams m_menuParams;

    /**
     * 记录手指按下时的横坐标。 
     */
    private float m_xDown;

    /**
     * 记录手指移动时的横坐标。 
     */
    private float m_xMove;

    /**
     * 记录手机抬起时的横坐标。 
     */
    private float m_xUp;

    /**
     * menu当前是显示还是隐藏。只有完全显示或隐藏menu时才会更改此值，滑动过程中此值无效。 
     */
    private boolean m_isMenuVisible;

    /**
     * 用于计算手指滑动的速度。 
     */
    private VelocityTracker m_velocityTracker;
    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化一些值
        initValues();

        //设置监听器
        m_content.setOnTouchListener(this);
    }

    /**
     * 初始化一些关键性数据。包括获取屏幕的宽度，给content布局重新设置宽度，给menu布局重新设置宽度和偏移距离等。 
     */
    private void initValues() {

        //获得窗口的宽度
        WindowManager window                = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        m_screenWidth                       = window.getDefaultDisplay().getWidth();

        //获得中间部分的内容
        m_content                           = findViewById(R.id.m_content);
        //获得菜单栏
        m_menu                              = findViewById(R.id.m_menu);

        //获得菜单栏的布局参数
        m_menuParams                        = (LinearLayout.LayoutParams) m_menu.getLayoutParams();

        // 将menu的宽度设置为屏幕宽度减去menuPadding
        m_menuParams.width                  = m_screenWidth - m_menuPadding;

        // 左边缘的值赋值为menu宽度的负数
        m_leftEdge                          = -m_menuParams.width;

        // menu的leftMargin设置为左边缘的值，这样初始化时menu就变为不可见
        m_menuParams.leftMargin             = m_leftEdge;

        // 将content的宽度设置为屏幕宽度
        m_content.getLayoutParams().width   = m_screenWidth;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        createVelocityTracker(event);

        switch (event.getAction()) {
            ////////////////////////////////////////////////////////////////////////////////////////
            case MotionEvent.ACTION_DOWN:

                // 手指按下时，记录按下时的横坐标
                m_xDown = event.getRawX();
                break;
            ////////////////////////////////////////////////////////////////////////////////////////

            case MotionEvent.ACTION_MOVE:

                // 手指移动时，对比按下时的横坐标，计算出移动的距离
                // 来调整menu的leftMargin值，从而显示和隐藏menu
                m_xMove = event.getRawX();
                int distanceX = (int) (m_xMove - m_xDown);

                //如果左边的窗口可见  那么滑出来的时候 leftMargin就是滑动的距离 此时为负数
                if (m_isMenuVisible) {
                    m_menuParams.leftMargin = distanceX;
                }
                //同理
                else {
                    m_menuParams.leftMargin = m_leftEdge + distanceX;
                }

                //如果到了下界
                if (m_menuParams.leftMargin < m_leftEdge) {
                    m_menuParams.leftMargin = m_leftEdge;
                }
                //如果到了上界 就都保持不便
                else if (m_menuParams.leftMargin > m_rightEdge) {
                    m_menuParams.leftMargin = m_rightEdge;
                }

                //设置菜单的布局参数
                m_menu.setLayoutParams(m_menuParams);
                break;
            ////////////////////////////////////////////////////////////////////////////////////////

            case MotionEvent.ACTION_UP:

                // 手指抬起时，进行判断当前手势的意图
                // 从而决定是滚动到menu界面，还是滚动到content界面
                m_xUp = event.getRawX();

                if (wantToShowMenu()) {
                    if (shouldScrollToMenu()) {
                        scrollToMenu();
                    } else {
                        scrollToContent();
                    }
                }

                else if (wantToShowContent()) {
                    if (shouldScrollToContent()) {
                        scrollToContent();
                    } else {
                        scrollToMenu();
                    }
                }

                recycleVelocityTracker();
                break;
        }

        return true;
    }

    /**
     * 判断当前手势的意图是不是想显示content。如果手指移动的距离是负数，且当前menu是可见的，则认为当前手势是想要显示content。 
     *
     * @return 当前手势想显示content返回true，否则返回false。 
     */
    private boolean wantToShowContent() {
        return m_xUp - m_xDown < 0 && m_isMenuVisible;
    }

    /**
     * 判断当前手势的意图是不是想显示menu。如果手指移动的距离是正数，且当前menu是不可见的，则认为当前手势是想要显示menu。 
     *
     * @return 当前手势想显示menu返回true，否则返回false。 
     */
    private boolean wantToShowMenu() {
        return m_xUp - m_xDown > 0 && !m_isMenuVisible;
    }

    /**
     * 判断是否应该滚动将menu展示出来。如果手指移动距离大于屏幕的1/2，或者手指移动速度大于SNAP_VELOCITY， 
     * 就认为应该滚动将menu展示出来。 
     *
     * @return 如果应该滚动将menu展示出来返回true，否则返回false。 
     */
    private boolean shouldScrollToMenu() {

        return (m_xUp - m_xDown > m_screenWidth / 2) ||
                (getScrollVelocity() > SNAP_VELOCITY);
    }

    /**
     * 判断是否应该滚动将content展示出来。如果手指移动距离加上menuPadding大于屏幕的1/2， 
     * 或者手指移动速度大于SNAP_VELOCITY， 就认为应该滚动将content展示出来。 
     *
     * @return 如果应该滚动将content展示出来返回true，否则返回false。 
     */
    private boolean shouldScrollToContent() {

        return (m_xDown - m_xUp + m_menuPadding > m_screenWidth / 2) ||
                (getScrollVelocity() > SNAP_VELOCITY);
    }

    /**
     * 将屏幕滚动到menu界面，滚动速度设定为30. 
     */
    private void scrollToMenu() {

        new ScrollTask().execute(30);
    }

    /**
     * 将屏幕滚动到content界面，滚动速度设定为-30. 
     */
    private void scrollToContent() {

        new ScrollTask().execute(-30);
    }

    /**
     * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。 
     *
     * @param event
     *            content界面的滑动事件 
     */
    private void createVelocityTracker(MotionEvent event) {

        if (m_velocityTracker == null) {
            m_velocityTracker = VelocityTracker.obtain();
        }

        m_velocityTracker.addMovement(event);
    }

    /**
     * 获取手指在content界面滑动的速度。 
     *
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。 
     */
    private int getScrollVelocity() {

        m_velocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) m_velocityTracker.getXVelocity();

        return Math.abs(velocity);
    }

    /**
     * 回收VelocityTracker对象。 
     */
    private void recycleVelocityTracker() {

        m_velocityTracker.recycle();

        m_velocityTracker = null;
    }

    class ScrollTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... speed) {

            int leftMargin = m_menuParams.leftMargin;

            // 根据传入的速度来滚动界面，当滚动到达左边界或右边界时，跳出循环。
            while (true) {

                leftMargin = leftMargin + speed[0];
                if (leftMargin > m_rightEdge) {
                    leftMargin = m_rightEdge;
                    break;
                }

                if (leftMargin < m_leftEdge) {
                    leftMargin = m_leftEdge;
                    break;
                }

                publishProgress(leftMargin);
                // 为了要有滚动效果产生，每次循环使线程睡眠20毫秒，这样肉眼才能够看到滚动动画。  
                sleep(20);
            }
            if (speed[0] > 0) {
                m_isMenuVisible = true;
            } else {
                m_isMenuVisible = false;
            }
            return leftMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... leftMargin) {
            m_menuParams.leftMargin = leftMargin[0];
            m_menu.setLayoutParams(m_menuParams);
        }

        @Override
        protected void onPostExecute(Integer leftMargin) {
            m_menuParams.leftMargin = leftMargin;
            m_menu.setLayoutParams(m_menuParams);
        }
    }

    /**
     * 使当前线程睡眠指定的毫秒数。 
     *
     * @param millis
     *            指定当前线程睡眠多久，以毫秒为单位 
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
