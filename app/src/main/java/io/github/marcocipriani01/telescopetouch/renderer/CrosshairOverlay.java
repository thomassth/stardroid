package io.github.marcocipriani01.telescopetouch.renderer;

import android.content.res.Resources;

import javax.microedition.khronos.opengles.GL10;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.renderer.util.SearchHelper;
import io.github.marcocipriani01.telescopetouch.renderer.util.TextureManager;
import io.github.marcocipriani01.telescopetouch.renderer.util.TextureReference;
import io.github.marcocipriani01.telescopetouch.renderer.util.TexturedQuad;
import io.github.marcocipriani01.telescopetouch.units.Vector3;

public class CrosshairOverlay {

    private TexturedQuad mQuad = null;
    private TextureReference mTex = null;

    public void reloadTextures(GL10 gl, Resources res, TextureManager textureManager) {
        // Load the crosshair texture.
        mTex = textureManager.getTextureFromResource(gl, R.drawable.crosshair);
    }

    public void resize(GL10 gl, int screenWidth, int screenHeight) {
        mQuad = new TexturedQuad(mTex,
                0, 0, 0,
                40.0f / screenWidth, 0, 0,
                0, 40.0f / screenHeight, 0);
    }

    public void draw(GL10 gl, SearchHelper searchHelper, boolean nightVisionMode) {
        // Return if the label has a negative z.
        Vector3 position = searchHelper.getTransformedPosition();
        if (position.z < 0) {
            return;
        }

        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glTranslatef(position.x, position.y, 0);

        int period = 1000;
        long time = System.currentTimeMillis();
        float intensity = 0.7f + 0.3f * (float) Math.sin((time % period) * 2f * (float) Math.PI / period);
        if (nightVisionMode) {
            gl.glColor4f(intensity, 0, 0, 0.7f);
        } else {
            gl.glColor4f(intensity, intensity, 0, 0.7f);
        }

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        mQuad.draw(gl);

        gl.glDisable(GL10.GL_BLEND);

        gl.glPopMatrix();
    }
}
