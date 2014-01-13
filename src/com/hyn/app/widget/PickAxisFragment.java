package com.hyn.app.widget;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.*;
import android.widget.TextView;
import com.hyn.app.R;
import com.hyn.app.util.SLog;
import com.hyn.app.util.SoulException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yananh
 * Date: 13-11-21
 * Time: 上午11:23
 * This axis line that user can select the start position and end position. it's provide a
 * convenience way for user to select time fragment.
 * <br/>
 * A complete axis line as value of one. The leftest pixel of the set value to zero and the
 * rightest of the line set the value to one. But it's not means that set the value in
 * proportion. For example, the middle of the line do not set value 0.5 in sometimes,
 * it's may be 0.4 or 0.6, it even has the potential to 0.8... which decided by #{@link Accelerator#}.
 *
 */
public class PickAxisFragment extends Fragment{
    private final static String TAG = PickAxisFragment.class.getSimpleName();

    /**
     * The key for a float parameter in the fragment's Intent bundle to indicate the default left position
     * when fragment create at the first time.
     */
    public static final String LEFT_POSITION_BUNDLE_KEY = "com.hyn.app.widget.PickAxisFragment.LeftPosition";
    /**
     * The key for a float parameter in the fragment's Intent bundle to indicate the default right position
     * when fragment create at the first time.
     */
    public static final String RIGHT_POSITION_BUNDLE_KEY = "com.hyn.app.widget.PickAxisFragment.RightPosition";
    /**
     * The key for a boolean parameter in the fragment's Intent bundle to indicate the default right position
     * when fragment create at the first time.
     */
    public static final String DRAW_BACKGROUND_BUNDLE_KEY = "com.hyn.app.widget.PickAxisFragment.DrawBackground";


    /** Default left position in the axis line. */
    private static final float DEFAULT_LEFT_POSITION = 0.7F;
    /** Default right position in the axis line. */
    private static final float DEFAULT_RIGHT_POSITION = 0.75F;
    /** Default value which indicate if need to draw background. */
    private static final boolean DEFAULT_DRAW_BACKGROUND =false;

    private float mDrawableLeftAxisPosition = DEFAULT_LEFT_POSITION;
    private float mDrawableRightAxisPosition = DEFAULT_RIGHT_POSITION;
    private boolean isDrawBackground = true;
    private float mInternalLeftAxisPosition = mDrawableLeftAxisPosition;
    private float mInternalRightAxisPosition = mDrawableRightAxisPosition;
    private View mBackGroundView = null;
    private TextView mLeftIndicatorView = null;
    private TextView mRightIndicatorView = null;
    private View mLeftPointView = null;
    private View mRightPointView = null;
    private ViewGroup mViewGroup = null;
    private int mParentRawX = -1;
    private int mParentWidth = -1;
    private int mLeftestRawX = -1;
    private int mRightestRawX = -1;
    private int mVisibleWidth = -1;
    private ProgressiveDrawable mProgressiveDrawable;
    private Accelerator mAccelerator = mDefaultDecelerator;
    /** a set of listener. */
    ListenerInfo mListenerInfo = null;
    /** get the description, if not set this var, then do not display the upper descriptor view. */
    DescriptorAdapter mDescriptor = null;
    public PickAxisFragment() {
        initFragment();
    }

    public PickAxisFragment(Bundle data) {
        mInternalLeftAxisPosition = data.getFloat(LEFT_POSITION_BUNDLE_KEY, DEFAULT_LEFT_POSITION);
        mInternalRightAxisPosition = data.getFloat(RIGHT_POSITION_BUNDLE_KEY, DEFAULT_RIGHT_POSITION);
        isDrawBackground = data.getBoolean(DRAW_BACKGROUND_BUNDLE_KEY, DEFAULT_DRAW_BACKGROUND);
        initFragment();
    }

    void initFragment(){
        setAccelerator(mDefaultDecelerator);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SLog.i(TAG, "onCreate");
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attr, Bundle savedInstanceState) {
        super.onInflate(activity, attr, savedInstanceState);
        SLog.i(TAG, "onInflate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.pick_fragment, container, false);
        mBackGroundView = view.findViewById(R.id.pick_fragment_background);
        mLeftIndicatorView = (TextView)view.findViewById(R.id.pick_left_indicator);
        mRightIndicatorView = (TextView)view.findViewById(R.id.pick_right_indicator);
        mLeftPointView = view.findViewById(R.id.pick_left_point);
        mRightPointView = view.findViewById(R.id.pick_right_point);
        mViewGroup = view;
        mLeftIndicatorView.setVisibility(View.VISIBLE);
        mRightIndicatorView.setVisibility(View.VISIBLE);

        view.post(new Runnable() {
            @Override
            public void run() {
                initPointViews();
                if(isDrawBackground){
                    drawDrawBackGround();
                }
            }
        });

        return view;
    }


    private void initPointViews(){
        getParentRawX();
        setupPointPosition();
        setupPointViewTouchDelegate(mLeftPointView, mPointViewFilter, new OnDragEvent() {
            @Override
            public void onDrag(MotionEvent event) {
                float newX = event.getRawX() - getParentRawX();
                updateNewLeftPointViewPosition(newX, event.getAction() == MotionEvent.ACTION_UP);
            }
        });
        setupPointViewTouchDelegate(mRightPointView, mPointViewFilter, new OnDragEvent() {
            @Override
            public void onDrag(MotionEvent event) {
                float newX = event.getRawX() - getParentRawX();
                updateNewRightPointViewPosition(newX, event.getAction() == MotionEvent.ACTION_UP);
            }
        });
    }

    /**
     * Setup position of the point view. It's be called when fragment be create at the first time.
     */
    final void setupPointPosition(){
        float left = mDrawableLeftAxisPosition;
        float right = mDrawableRightAxisPosition;
        int width = getVisibleWidth();
        mLeftPointView.setTranslationX(left * width + getLeftestRawX());
        mRightPointView.setTranslationX(right * width + getLeftestRawX());
        onPositionMove(mLeftPointView, mLeftIndicatorView, left);
        onPositionMove(mRightPointView, mRightIndicatorView, right);
        onPickedChange(mInternalLeftAxisPosition, mInternalRightAxisPosition);
    }


    private static void setupPointViewTouchDelegate(final View delegateView, final EventFilter filter,
                                                    final OnDragEvent dragEvent){
        delegateView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(filter.filter(event)){
                    dragEvent.onDrag(event);
                }
                return true;
            }
        });
        View parent = (View) delegateView.getParent();
        Rect r = new Rect();
        delegateView.getHitRect(r);
        r.top -= 10;
        r.bottom += 10;
        r.left -= 30;
        r.right += 30;
        parent.setTouchDelegate(new TouchDelegate(r, delegateView));
    }


    /**
     * Called when user drag the point view, the selected fragment has changed.
     * */
    void onPositionMove(View pointView, TextView indicatorView, float newPosition){
        indicatorView.setTranslationX(pointView.getTranslationX());
        newPosition = newPosition<0.00001F?0:newPosition;
        newPosition = newPosition>0.99999F?1:newPosition;
        newPosition = getAcceleratedRatio(newPosition);
        indicatorView.setText(getDescriptor(newPosition));
        if(pointView == mLeftPointView){
            //update left point view
            mInternalLeftAxisPosition = newPosition;
        }else if(pointView == mRightPointView){
            //update right point view
            mInternalRightAxisPosition = newPosition;
        }
    }

    void updateNewLeftPointViewPosition(float newTransitionX, boolean notify){
        float newX = newTransitionX;
        newX = newX > getRightestRawX()?getRightestRawX():newX;
        newX = newX < getLeftestRawX()?getLeftestRawX():newX;
        float rightX = mRightPointView.getTranslationX();
        float interval = mLeftPointView.getWidth() + 20;

        if (rightX <= newX + interval){
            newX = rightX - interval;
        }

        mLeftPointView.setTranslationX(newX);
        onPositionMove(mLeftPointView, mLeftIndicatorView, (newX-getLeftestRawX()) / getVisibleWidth());
        if(notify){
            onPickedChange(mInternalLeftAxisPosition, mInternalRightAxisPosition);
        }
    }

    /**
     * update a new position and update some information.
     * @param newTransitionX a relate to parent's left coordinate.
     * @param notify if need to notify all callbacks.
     */
    void updateNewRightPointViewPosition(float newTransitionX, boolean notify){
        float newX = newTransitionX;
        newX = newX > getRightestRawX()?getRightestRawX():newX;
        newX = newX < getLeftestRawX()?getLeftestRawX():newX;
        float leftX = mLeftPointView.getTranslationX();
        float interval = mLeftPointView.getWidth() + 20;

        if (leftX + interval > newX ){
            newX = leftX + interval;
        }

        mRightPointView.setTranslationX(newX);
        onPositionMove(mRightPointView, mRightIndicatorView, (newX-getLeftestRawX()) / getVisibleWidth());
        if (notify) {
            onPickedChange(mInternalLeftAxisPosition, mInternalRightAxisPosition);
        }
    }

    int getVisibleWidth(){
        return getVisibleWidth(false);
    }

    private int getVisibleWidth(boolean isForceRefresh){
        if(isForceRefresh || mVisibleWidth < 0){
            mVisibleWidth = getRightestRawX(true) - getLeftestRawX(true);
        }
        return mVisibleWidth;
    }

    int getParentRawX(){
        return getParentRawX(false);
    }

    private int getParentRawX(boolean isForceRefresh){
        if(isForceRefresh || mParentRawX < 0){
            ViewParent p = mRightPointView.getParent();
            if(null == p) throw new SoulException("Cannot get parent's location in screen!");
            if(!ViewGroup.class.isInstance(p)){
                throw new SoulException("Parent is un-normal.");
            }
            ViewGroup pv = (ViewGroup)p;
            int pi[] = new int[4];
            pv.getLocationOnScreen(pi);
            mParentRawX = pi[0];
        }
        return mParentRawX;
    }

    int getParentWidth(){
        return getParentWidth(false);
    }

    private int getParentWidth(boolean isForceRefresh){
        if(isForceRefresh || mParentWidth < 0){
            mParentWidth = mViewGroup.getWidth();
        }
        return mParentWidth;
    }

    int getLeftestRawX(){
        return getLeftestRawX(false);
    }

    private int getLeftestRawX(boolean isForceRefresh){
        if(isForceRefresh || mLeftestRawX < 0){
            mLeftestRawX = getActivity().getResources().getDimensionPixelSize(R.dimen.pick_fragment_background_padding);
            mLeftestRawX -= getLeftPointViewSize()[0]/1.5F;
        }
        return mLeftestRawX;
    }

    int getRightestRawX(){
        return getRightestRawX(false);
    }

    private int getRightestRawX(boolean isForceRefresh){
        if(isForceRefresh || mRightestRawX < 0){
            mRightestRawX = getParentWidth(true) - getActivity().getResources().getDimensionPixelSize(R.dimen.pick_fragment_background_padding);
            mRightestRawX -= 10;
        }
        return mRightestRawX;
    }


    private int[] getLeftPointViewSize(){
        return new int[]{mLeftPointView.getWidth(), mLeftPointView.getHeight()};
    }

    private int[] getRightPointViewSize(){
        return new int[]{mRightPointView.getWidth(), mRightPointView.getHeight()};
    }




    void onPickedChange(float left, float right){
        getListenerInfo().notifyPick(left, right);
    }


    String getDescriptor(float ratio){
        if(null == mDescriptor){
            return null;
        }
        Object obj = mDescriptor.description(ratio);
        if(null == obj) return null;
        return obj.toString();
    }

    float getAcceleratedRatio(float ratio){
        if(null == mAccelerator){
            return ratio;
        }
        return mAccelerator.transform(ratio);
    }

    public void setPickedListener(PickFragmentCallback listen){
        getListenerInfo().addListener(listen);
    }

    public void removePickedListener(PickFragmentCallback listen){
        getListenerInfo().removeListener(listen);
    }

    private void drawDrawBackGround(){
        if(null == mProgressiveDrawable){
            mProgressiveDrawable = new ProgressiveDrawable(mBackGroundView.getWidth(), mBackGroundView.getHeight(), getAccelerator());
            mProgressiveDrawable.setAlpha(200);
        }
        mBackGroundView.setBackground(mProgressiveDrawable);
    }

    public void setDescriptorAdapter(DescriptorAdapter descriptor){
        mDescriptor = descriptor;
    }

    Accelerator getAccelerator() {
        return mAccelerator;
    }

    /**
     * change a new accelerator for the
     * @param accelerator
     */
    public void setAccelerator(Accelerator accelerator) {
        mAccelerator = accelerator;
//        if(null != mProgressiveDrawable){
//            mProgressiveDrawable.updateAccelerator(accelerator);
//        }
    }

    /**
     * This function will be called when orientation changed. During this function have to
     * re-initialize this fragment again. In this function should do things as follow:
     * 1, setup the position of the left/right point view;
     * 2, change the background if need;
     * 3, update the #{@link #getParentRawX()} and #{@link #getParentWidth()} ;
     * 4, notify the selected
     * @param newConfig the newer configure
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);

        //TODO
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        SLog.i(TAG, "onDetach");
    }

    @Override
    public void onStop() {
        super.onStop();
        SLog.i(TAG, "onStop");
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        SLog.i(TAG, "setArguments");
    }

    EventFilter mPointViewFilter = new EventFilter(){
        @Override
        public boolean filter(MotionEvent event) {
            SLog.d(TAG, "filter x = " + event.getRawX());
            if(mRightPointView.getTranslationX() >= mLeftPointView.getTranslationX()+10){
                return true;
            }
            return false;
        }
    };

    static Accelerator mDefaultDecelerator = new Accelerator(){
        private static final float THETA = 30;
        private final float TAN_THETA = (float)Math.tan(THETA);
        private final float AREA = TAN_THETA / 2;
        @Override
        public float transform(float ratio) {
            float xh = TAN_THETA * (1 - ratio);
            float h = TAN_THETA;
            return (xh+h)*ratio/2/AREA;
        }
    };

    /**
     * a listener set class to manager all listeners.
     */
    final public static class ListenerInfo{
        final List<PickFragmentCallback> mPickFragmentCallbacks = new ArrayList<PickFragmentCallback>();
        /** Add listener to listen the picked fragment. */
        synchronized void addListener(PickFragmentCallback listen){
            mPickFragmentCallbacks.add(listen);
        }
        /** remove listener to abandon listen the picked fragment. */
        synchronized void removeListener(PickFragmentCallback listen){
            if(null == listen){
                mPickFragmentCallbacks.clear();
            }else{
                mPickFragmentCallbacks.remove(listen);
            }
        }

        /**
         * When user selected the target, notify all listeners update a new target.
         * @param left left part in axis line.
         * @param right right part in axis line.
         */
        void notifyPick(float left, float right){
            List<PickFragmentCallback> callbacks = new ArrayList<PickFragmentCallback>();
            synchronized (this){
                callbacks.addAll(mPickFragmentCallbacks);
            }
            for(PickFragmentCallback callback : callbacks){
                callback.handle(left, right);
            }
        }
    }

    final ListenerInfo getListenerInfo(){
        if(null == mListenerInfo){
            mListenerInfo = new ListenerInfo();
        }
        return mListenerInfo;
    }

    /***
     * This class is a drawable for the background view.
     */
    static class ProgressiveDrawable extends Drawable {
        private static final int ALPHA_KEY = 0x01;
        private static final int COLOR_KEY = 0x02;
        int mPixelWidth;
        int mHeight = 1;
        int mLeftColor = Color.BLUE;
        int mRightColor = Color.RED;
        int mAlpha = 255;
        int mColor[] = null;
        Accelerator mAccelerator;
        ProgressiveDrawable(int pixelWidth, int height, Accelerator accelerator){
            mAccelerator = accelerator;
            setBounds(0, 0, 0, 0);
            onSizeChange(pixelWidth, height);
        }

        /**
         * Called when size background view size changed.
         * @param pixelWidth background view's width
         * @param pixelHeight background view's height
         */
        public void onSizeChange(int pixelWidth, int pixelHeight){
            mPixelWidth = pixelWidth;
            mHeight = pixelHeight;

            mColor = new int[mPixelWidth];
            reset(COLOR_KEY | ALPHA_KEY);
        }

        /**
         * Change a new accelerator
         * @param accelerator new accelerator
         */
        public void updateAccelerator(Accelerator accelerator){
            mAccelerator = accelerator;
            reset(COLOR_KEY | ALPHA_KEY);
        }

        /**
         * update the color array.
         * @param change indicate that if it's a size change or alpha change or both.
         */
        private  void reset(int change){
            int alpha = ((mAlpha&0x00FF)<<24);
            int r;
            int g;
            int b;
            int c1 = mLeftColor;
            int c2 = mRightColor;
            int count = mPixelWidth;
            int [] color = mColor;
            Accelerator accelerator = mAccelerator;
            SLog.d("ddd", "reset change = "+change);
            if((change & COLOR_KEY) != 0){
                //color[i] = (rightColor - leftColor) * ratio + leftColor;
                int redInterval = ((0x00FF0000&c2)>>16) - ((0x00FF0000&c1)>>16);
                int redLeftColor = (0x00FF0000&c1)>>16;
                int greenInterval = ((0x0000FF00&c2)>>8) - ((0x0000FF00&c1)>>8);
                int greenLeftColor = (0x0000FF00&c1)>>8;
                int blueInterval = (0x000000FF&c2) - (0x000000FF&c1);
                int blueLeftColor = 0x000000FF&c1;

                for(int i=0;i<count;++i){
                    float ratio = accelerator.transform((float)i/count);
                    r = ((int)(redInterval * ratio) + redLeftColor) & 0x00FF;
                    g = ((int)(greenInterval * ratio) + greenLeftColor) & 0x00FF;
                    b = ((int)(blueInterval * ratio) + blueLeftColor) & 0x00FF;
                    color[i] = alpha | (r<<16) | (g<<8) | b;
                }
            }

            if((change & ALPHA_KEY) != 0){
                for(int i=0;i<count;++i){
                    color[i] = (color[i]&0x00FFFFFF) | alpha;
                }
            }
        }


        @Override
        public void draw(Canvas canvas) {
            for(int i =0;i<mHeight;++i){
                canvas.drawBitmap(mColor, 0, mPixelWidth, 0, i,mPixelWidth, 1, true, null);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            mAlpha = alpha;
            reset(ALPHA_KEY);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {TextView TV;
            throw new SoulException("setColorFilter is invalid to ProgressiveDrawable!");
        }

        @Override
        public int getOpacity() {
            return PixelFormat.RGBA_8888;
        }
    }


    /**
     * Callback interface that will be called when user drag the delegated view, it check if the
     * event could be consumed by delegated view.
     */
    interface EventFilter{
        /**
         * Check if the event could be consumed by the delegated view.
         * @param event the new event should be checked.
         * @return boolean indicate if the event if valid. true means that the event is valid, false
         * means that it's invalid.
         */
        public boolean filter(MotionEvent event);
    }

    /**
     * Callback interface that will be called when user drag the delegated view.
     */
    interface OnDragEvent{
        void onDrag(MotionEvent event);
    }


    /**
     * It's a map between visible position with real output offset.
     * For example, if it's a constrain proportions, usually they have the same
     * value, the output value of he middle of the axis line is 0.5F. But in the
     * un-constrain proportions' condition, usually they has the difference value.
     * It's recommend to use a un-constrain proportion accelerator rather than a
     * constrain proportions, especially decelerator, it's a better interaction between
     * user and view.
     * <br/>
     * <b>Note: </b>user could change the Accelerator for this fragment to improve the
     * interaction, but must observe some rules. 1, the output must be a float value between
     * 0 to one; 2, different input must match different output;      *
     */
    public interface Accelerator {
        /**
         * transform from visible position to real offset.
         * @param ratio position in the line
         * @return float target position.
         */
        public float transform(float ratio);
    }

    /**
     * Callback interface that will be called when user select the target fragment.
     */
    public interface PickFragmentCallback{
        /**
         * Called when user select a invalid fragment.
         * @param left start position, between 0 to 1.
         * @param right end position, between 0 to 1.
         */
        public void handle(float left, float right);
    }

    /**
     * Get the description for a position. For example, if the axis line mean distance from
     * 0-1000, when the position is 0.5, the description could be "500M". If it's a time axis,
     * and set region from 13-11-21 00:00 to 13-11-22 00:00, when point to the middle of the line,
     * the description will be "13-11-21 12:00".
     * <br/>
     * If you want to get the more information about how to get the position and what the mean of the
     * position, please see @see PickAxisFragment.
     */
    public interface DescriptorAdapter{
        /**
         * Get description for the picked position.
         * @param position the position on the line, @see PickFragmentCallback
         * @return a object which could get the description.
         */
        public Object description(float position);
    }
}
