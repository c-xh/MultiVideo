package com.demo.cx1.myui.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by cx1 on 2017/7/3.
 */

public class GLFrameSurface extends GLSurfaceView {

    public GLFrameSurface(Context context) {
        super(context);
    }

    public GLFrameSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // setRenderMode() only takes effectd after SurfaceView attached to window!
        // note that on this mode, surface will not render util GLSurfaceView.requestRender() is
        // called, it's good and efficient -v-
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
