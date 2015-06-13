# SlideMenu
仿人人客户端实现

细心的朋友玩人人的时候就会发现，他的客户端实现一开始是看不到菜单栏的
只有当你滑动窗口后才能看见

# Demo
![image](https://github.com/ChanJLee/SlideMenu/raw/master/app/demo.gif)

# Usage
1: 创建一个布局文件 
一如：
	<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:tools="http://schemas.android.com/tools"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:orientation="horizontal">
    	<LinearLayout
        	android:layout_width="fill_parent"
        	android:layout_height="fill_parent"
        	android:id="@+id/m_menu"
        	android:background="#ff4c84ff"
        	android:orientation="horizontal">
    	</LinearLayout>

    	<LinearLayout
        	android:layout_width="fill_parent"
        	android:layout_height="fill_parent"
        	android:id="@+id/m_content"
        	android:orientation="horizontal"
        	android:background="#fff">
    	</LinearLayout>
	</LinearLayout>
	
其中 id 为m_menu的存放菜单栏neir 一如图片中的白色部分
id 为m_content存放主体界面  一如图片中的白色部分

activity 中的使用参考demo 代码

