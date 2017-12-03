package peneder.mlh.gjovik.ntnu.no.everydayhero;

/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.List;
import java.util.Random;

import peneder.mlh.gjovik.ntnu.no.everydayhero.camera.GraphicOverlay;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float GENERIC_POS_OFFSET = 20.0f;
    private static final float GENERIC_NEG_OFFSET = -20.0f;

    private static final float BOX_STROKE_WIDTH = 5.0f;
    private static final String TAG = "FaceGraphic";

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;
    private final Bitmap starBitmap;
    private final Bitmap batmanBitmap;
    private final Bitmap blackWidowBitmap;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;
    private Bitmap bitmap;
    private Bitmap op;
    private int mFaceMaskId;
    private Bitmap mustacheBitmap;

    private boolean mIsMouthOpen = false;

    FaceGraphic(GraphicOverlay overlay, int faceMaskId) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
        mFaceMaskId = faceMaskId;
        bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), mFaceMaskId);
        starBitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.star);
        batmanBitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.batarang);
        mustacheBitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.mustache);
        blackWidowBitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.black_widow_spider);
        op = bitmap;
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        float faceWidth = face.getWidth();
        if (mFaceMaskId == R.drawable.black_widow_hair) {
            op = Bitmap.createScaledBitmap(bitmap, (int) scaleX(faceWidth+140),
                    (int) scaleY(((bitmap.getHeight() * (faceWidth+160)) / bitmap.getWidth())), false);
        } else if (mFaceMaskId == R.drawable.batman_edit) {
            op = Bitmap.createScaledBitmap(bitmap, (int) scaleX(faceWidth-40),
                    (int) scaleY(((bitmap.getHeight() * (faceWidth)) / bitmap.getWidth())), false);
        } else {
            op = Bitmap.createScaledBitmap(bitmap, (int) scaleX(faceWidth),
                    (int) scaleY(((bitmap.getHeight() * faceWidth) / bitmap.getWidth())), false);
        }
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;

        //edit placement for batman mask
        if (mFaceMaskId == R.drawable.batman_edit) {
            top -= 350;
            left += 50;
        } else if (mFaceMaskId == R.drawable.black_widow_hair) {
            top -= 180;
            left -= 120;
        }
        Random r = new Random();
        if (face.getLandmarks().size() > 7 && (contains(face.getLandmarks(), Landmark.RIGHT_EYE) != 99)
                && (contains(face.getLandmarks(), Landmark.RIGHT_CHEEK) != 99)
                && mFaceMaskId == R.drawable.black_widow_hair) {
            PointF mouthL = face.getLandmarks().get(Landmark.LEFT_CHEEK).getPosition();
            PointF mouthR = face.getLandmarks().get(Landmark.RIGHT_CHEEK).getPosition();
            int mouthLX = (int) mouthL.x;
            int mouthRX = (int) mouthR.x;
            //use average y
            int mouthY = (int) ((mouthR.y+mouthL.y) /2);
            for (int i = 0; i < 5; i++) {
                int i1 = r.nextInt(mouthLX - (mouthRX)) + mouthRX;
                int i2 = r.nextInt(mouthY - (mouthY - 50)) + mouthY;
//            int i3 = r.nextInt( (int) rightEye.x - (int) (rightEye.x-40)) + (int) rightEye.x;
//            int i4 = r.nextInt( (int) rightEye.y - (int) (rightEye.y-40)) + (int) rightEye.y;
//            canvas.drawBitmap(starBitmap, i1, i2, new Paint());
                canvas.drawBitmap(starBitmap, scaleX(i1), scaleY(i2), new Paint());
            }
        }
        float right = x + xOffset;
        float bottom = y + yOffset;
        //op = rotateBitmap(op, face.getEulerZ());

        if ((contains(face.getLandmarks(), 11) != 99)
                && (contains(face.getLandmarks(), 5) != 99)
                && (contains(face.getLandmarks(), 0) != 99)
                ) {

            Log.i(TAG, "draw: Mouth Open >> found all the points");

            /**
             * for bottom mouth
             */
            int cBottomMouthX;
            int cBottomMouthY;
            cBottomMouthX = (int) translateX(face.getLandmarks().get(contains(face.getLandmarks(), 0)).getPosition().x);
            cBottomMouthY = (int) translateY(face.getLandmarks().get(contains(face.getLandmarks(), 0)).getPosition().y);

            Log.i(TAG, "draw: Condition Bottom mouth >> cBottomMouthX >> " + cBottomMouthX + "    cBottomMouthY >> " + cBottomMouthY);

            //canvas.drawCircle(cBottomMouthX, cBottomMouthY, 10, new Paint());

            /**
             * for left mouth
             */
            int cLeftMouthX;
            int cLeftMouthY;
            cLeftMouthX = (int) translateX(face.getLandmarks().get(contains(face.getLandmarks(), 5)).getPosition().x);
            cLeftMouthY = (int) translateY(face.getLandmarks().get(contains(face.getLandmarks(), 5)).getPosition().y);

            Log.i(TAG, "draw: Condition LEft mouth >> cLeftMouthX >> " + cLeftMouthX + "    cLeftMouthY >> " + cLeftMouthY);

            //canvas.drawCircle(cLeftMouthX, cLeftMouthY, 10, new Paint());

            /**
             * for Right mouth
             */
            int cRightMouthX;
            int cRightMouthY;
            cRightMouthY = (int) translateY(face.getLandmarks().get(contains(face.getLandmarks(), 11)).getPosition().y);
            cRightMouthX = (int) translateX(face.getLandmarks().get(contains(face.getLandmarks(), 11)).getPosition().x);

            Log.i(TAG, "draw: Condition Right mouth >> cRightMouthX >> " + cRightMouthX + "    cRightMouthY >> " + cRightMouthY);

            //canvas.drawCircle(cRightMouthX, cRightMouthY, 10, new Paint());

            float centerPointX = (cLeftMouthX + cRightMouthX) / 2;
            float centerPointY = ((cLeftMouthY + cRightMouthY) / 2) - 20;

            //canvas.drawCircle(centerPointX, centerPointY, 10, new Paint());

            float differenceX = centerPointX - cBottomMouthX;
            float differenceY = centerPointY - cBottomMouthY;

            Log.i(TAG, "draw: difference X >> " + differenceX + "     Y >> " + differenceY);

            if (differenceY < (-100)) {
                mIsMouthOpen = true;
                Log.i(TAG, "draw: difference Mouth is OPENED ");
                for (int i = 0; i < 5; i++) {
                    int i1 = r.nextInt(cRightMouthX - cLeftMouthX) + cLeftMouthX;
                    int i2 = (int) (r.nextInt((int) (centerPointY - (centerPointY- 50))) + centerPointY);
                    if (mFaceMaskId == R.drawable.batman_edit) {
                        canvas.drawBitmap(batmanBitmap, i1, i2, new Paint());
                    } else {
                        canvas.drawBitmap(starBitmap, i1, i2, new Paint());
                    }

                }
            } else {
                mIsMouthOpen = false;
                Log.i(TAG, "draw: difference Mouth is CLOSED ");
            }
        }

        // Draw the hat only if the subject's head is titled at a sufficiently jaunty angle.
        float eulerZ = face.getEulerZ();

        final float HEAD_TILT_HAT_THRESHOLD = 20.0f;
        if (Math.abs(eulerZ) > HEAD_TILT_HAT_THRESHOLD && mFaceMaskId == R.drawable.black_widow_hair) {

            for (int i = 0; i < 7; i++) {
                int i1 = (int) (r.nextInt((int) (x - left)) + left);
                int i2 = (int) (r.nextInt((int) (y - (top))) + top);
//            canvas.drawBitmap(starBitmap, i1, i2, new Paint());
                canvas.drawBitmap(blackWidowBitmap, i1, i2, new Paint());

            }
        }

        canvas.drawBitmap(op, left, top, new Paint());
    }

    private int contains(List<Landmark> list, int name) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getType() == name) {
                return i;
            }
        }
        return 99;
    }

    private float getNoseAndMouthDistance(PointF nose, PointF mouth) {
        return (float) Math.hypot(mouth.x - nose.x, mouth.y - nose.y);
    }


    public boolean isMouthOpen() {
        return mIsMouthOpen;
    }
}
