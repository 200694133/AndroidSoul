package com.hyn.app.page;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.WeakHashMap;
import com.hyn.app.util.SLog;
import com.hyn.app.util.SoulException;
import android.app.Activity;
import android.app.Fragment;

/**
 * This class is a flow line class to controller all fragment. when user change the fragment then do the fragment actions.
 * @author yananh
 *
 */
public class PageFlow {
	private static final String TAG = PageFlow.class.getSimpleName();
	public static enum Page{
		FootTrace, 
	}
	private static EnumMap<Page, Class<?>> sPageMap = new EnumMap<Page, Class<?>>(Page.class);
	static{
		sPageMap.put(Page.FootTrace, FootTracePage.class);
		sPageMap.put(Page.FootTrace, FootTracePage.class);
		sPageMap.put(Page.FootTrace, FootTracePage.class);
		sPageMap.put(Page.FootTrace, FootTracePage.class);
		sPageMap.put(Page.FootTrace, FootTracePage.class);
		sPageMap.put(Page.FootTrace, FootTracePage.class);
		sPageMap.put(Page.FootTrace, FootTracePage.class);
		sPageMap.put(Page.FootTrace, FootTracePage.class);
		sPageMap.put(Page.FootTrace, FootTracePage.class);
	}
	private List<Page> mPageList = new ArrayList<Page>();
	
	private static PageFlow mInstance = null;
	private Fragment mContentFragment = null;
	private Fragment mOptionPanelFragment = null;
	Activity mActivity;
	private WeakHashMap<Class<?>, IPage> mActivePageMap = new WeakHashMap<Class<?>, IPage>();
	
	public static synchronized PageFlow getInstance(Activity context){
		if(null == mInstance) mInstance = new PageFlow(context);
		return mInstance;
	}

	private PageFlow(Activity context){
		SLog.d(TAG, " create new FragmentFlow instance.");
		mActivity = context;
	}
	
	public void setup(Fragment contentFragment, Fragment optionPanel){
		mContentFragment = contentFragment;
		mOptionPanelFragment = optionPanel;
		mPageList.add(Page.FootTrace);
		mPageList.add(Page.FootTrace);
		mPageList.add(Page.FootTrace);
		mPageList.add(Page.FootTrace);
		mPageList.add(Page.FootTrace);
		mPageList.add(Page.FootTrace);
		mPageList.add(Page.FootTrace);
		mPageList.add(Page.FootTrace);
		mPageList.add(Page.FootTrace);
		mPageList.add(Page.FootTrace);
		mPageList.add(Page.FootTrace);
		mPageList.add(Page.FootTrace);
	}	
	
	public Page[] listPage(){
		return mPageList.toArray(new Page[]{});
	}
	
	public void displayPage(){
		displayPage(Page.FootTrace);
	}
	
	public void displayPage(Page type){
		Page nextType = type;
		IPage page = mActivePageMap.get(nextType);
		if(null == page){
			Class<?> clazz = sPageMap.get(nextType);
			try {
				page = (IPage)clazz.newInstance();
			} catch (InstantiationException e) {
				throw new SoulException("Create new page failed, InstantiationException "+e);
			} catch (IllegalAccessException e) {
				throw new SoulException("Create new page failed, IllegalAccessException "+e);
			} 
			mActivePageMap.put(clazz, page);
		}
		page.onCreate(mActivity, null);
		page.setup(mContentFragment, mOptionPanelFragment);
		page.agent();
	}
	
	
	
}
