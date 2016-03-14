package com.custom.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.administrator.simplerefreshlistview.R;

/**
 * @ Author: qiyue (ustory)
 * @ Email: qiyuekoon@foxmail.com
 * @ Data:2016/3/12
 */
public class SimpleRefreshListView extends ListView implements OnScrollListener {
    private View mHeaderView;
    private View mFooterView;
    private TextView mHeaderTextView;
    private TextView mFooterTextView;
    private OnRefreshListener mOnRefreshListener;
    private int mHeaderViewHeight;
    private int mFooterViewHeight;
    private float downY;
    private float difference;
    private int slow = 3;
    private int middle = 2;
    private int fast = 1;
    private int firstVisibleItemPosition = -1;
    private boolean isScrollToBottom ;
    private Float firstVisibleItemdown ;
    private int downFirstItemPosition;
    private boolean ableToPull;
    private int currentStatus ;
    private static final int STATUS_PULL_TO_REFRESH = 1;
    private static final int STATUS_RELEASE_TO_REFRESH = 2;
    private static final int REFRESHING = 3;
    private Animation upAnimation;
    private Animation downAnimation;
    private ImageView mImageView;
    private ProgressBar mProgressBar;


    public SimpleRefreshListView(Context context) {
        super(context);
        initView(context);
    }

    public SimpleRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SimpleRefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        mHeaderView = View.inflate(getContext(), R.layout.listview_header_view, null);
        mFooterView = View.inflate(getContext(), R.layout.listview_footer_view, null);
        mHeaderTextView = (TextView)mHeaderView.findViewById(R.id.header_tv);
        mFooterTextView = (TextView)mFooterView.findViewById(R.id.footer_tv);
        mImageView = (ImageView)mHeaderView.findViewById(R.id.imageView);
        mProgressBar = (ProgressBar) mHeaderView.findViewById(R.id.pb_listview_header);
        this.addHeaderView(mHeaderView);
        this.addFooterView(mFooterView);
        this.setOnScrollListener(this);
        //must to invoke ,getHeight only use Visiable View
        //measure(0,0)，according content mesure
        mHeaderView.measure(0, 0);
        mHeaderViewHeight = mHeaderView.getMeasuredHeight()+20;
        android.util.Log.i("qiyue","getMeasuredHeight="+mHeaderViewHeight);
        mHeaderView.setPadding(0,-mHeaderViewHeight,0,0);

        mFooterView.measure(0,0);
        mFooterViewHeight = mFooterView.getMeasuredHeight();
        mFooterView.setPadding(0,-mFooterViewHeight,0,0);
        initAnimation();

    }

    private void initAnimation() {
        upAnimation = new RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        upAnimation.setDuration(500);
        upAnimation.setFillAfter(true);

        downAnimation = new RotateAnimation(-180f, -360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        downAnimation.setDuration(500);
        downAnimation.setFillAfter(true);
    }


    public OnRefreshListener getOnRefreshListener() {
        return mOnRefreshListener;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        checkAblePull(ev);
        if (ableToPull && currentStatus!=REFRESHING) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downY = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float currentY = ev.getY();
                    difference = (currentY - downY) / slow;
                    int paddingTop = -mHeaderViewHeight + (int) difference;
                    if (difference > 0 && firstVisibleItemPosition == 0) {
                            if (paddingTop < 0) {
                                if (currentStatus == STATUS_RELEASE_TO_REFRESH){
                                    mImageView.startAnimation(downAnimation);
                                    mHeaderTextView.setText("下拉刷新");
                                }
                                currentStatus = STATUS_PULL_TO_REFRESH;
                                mHeaderView.setPadding(0, paddingTop, 0, 0);
                              //  mImageView.startAnimation(downAnimation);
                            } else if (currentStatus == STATUS_PULL_TO_REFRESH){
                                currentStatus = STATUS_RELEASE_TO_REFRESH;
                                mHeaderTextView.setText("松开刷新");
                                mImageView.startAnimation(upAnimation);
                            }
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
                        android.util.Log.i("qiyue", "RELEASE_REFRESH111");
                        currentStatus = REFRESHING;
                    }
                    handleEventByStatus(currentStatus);
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        firstVisibleItemPosition = firstVisibleItem;
        Log.i("qiyue","firstVisibleItem="+firstVisibleItem);
        if (getLastVisiblePosition() == (totalItemCount - 1)) {
            isScrollToBottom = true;
        } else {
            isScrollToBottom = false;
        }
    }


    private void checkAblePull(MotionEvent event) {
        View firstChild = this.getChildAt(0);
        if (firstChild != null) {
            int firstVisiblePos = this.getFirstVisiblePosition();
            if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
                // 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新
                if (!ableToPull) {
                    downY = event.getRawY();
                }
                ableToPull = true;
            } else {
                ableToPull = false;
            }
        } else {
            ableToPull = true;
        }
    }

    public void hideHeaderView(){
        mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
        mHeaderTextView.setText("下拉刷新");
        mImageView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        currentStatus = STATUS_PULL_TO_REFRESH;
    }

    public void handleEventByStatus(int status){
        switch (status){
            case STATUS_PULL_TO_REFRESH:
                //mImageView.startAnimation(downAnimation);
                hideHeaderView();
                break;
            case STATUS_RELEASE_TO_REFRESH:
               // mImageView.startAnimation(upAnimation);
                break;
            case REFRESHING:
                mImageView.clearAnimation();
                mImageView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mHeaderTextView.setText("刷新中....");
                mOnRefreshListener.onPullRefresh();
                break;
            default:
                break;

        }
    }

}
