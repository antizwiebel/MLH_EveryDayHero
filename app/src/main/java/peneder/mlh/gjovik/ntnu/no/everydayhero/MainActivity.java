package peneder.mlh.gjovik.ntnu.no.everydayhero;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.plattysoft.leonids.ParticleSystem;

import java.io.IOException;

import peneder.mlh.gjovik.ntnu.no.everydayhero.camera.CameraSourcePreview;
import peneder.mlh.gjovik.ntnu.no.everydayhero.camera.GraphicOverlay;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private Button mParticleView;
    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private int mFaceMaskID = R.drawable.captain_america;
    private View mParticleViewLeft;
    private View mParticleViewRight;

    private Activity mActivity;
    private View mParticleViewBottom;
    private ImageButton mBatmanButton;
    private TextView mTextView;
    private Superhero selectedHero = Superhero.CAPTAINAMERICA;
    private int mParticleID = R.drawable.captain_america_shield;
    private boolean mParticlesEmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        mParticleView = findViewById(R.id.particle_view);
        mParticleViewBottom = findViewById(R.id.emiter_bottom);
        mBatmanButton = findViewById(R.id.batman_image_button);
        mTextView = findViewById(R.id.text_view);
        //mParticleViewLeft = findViewById(R.id.particle_view_left);
        //mParticleViewRight = findViewById(R.id.particle_view_right);
        mActivity = this;
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }

    }

    public void toggleBatman(View view) {
        if (selectedHero != Superhero.BATMAN) {
            mParticleID = R.drawable.batarang;
            selectedHero = Superhero.BATMAN;
            mFaceMaskID = R.drawable.batman_edit;
            mCameraSource.release();
            createCameraSource();
            startCameraSource();
            new ParticleSystem(this, 100, mParticleID, 5000)
                .setSpeedRange(0.2f, 0.5f)
                .oneShot(mBatmanButton, 100);
        }
    }

    public void toggleCaptainAmerica(View view) {
        if (selectedHero != Superhero.CAPTAINAMERICA) {
            mParticleID = R.drawable.captain_america_shield;
            new ParticleSystem(this, 100, mParticleID, 5000)
                    .setSpeedRange(0.2f, 0.5f)
                    .oneShot(mTextView, 100);
            mTextView.setText("");
            selectedHero = Superhero.CAPTAINAMERICA;
            mFaceMaskID = R.drawable.captain_america;
            mCameraSource.release();
            createCameraSource();
            startCameraSource();
        }
    }

    public void toggleIronMan(View view) {
        if (selectedHero != Superhero.IRONMAN) {
            selectedHero = Superhero.IRONMAN;
            mFaceMaskID = R.drawable.iron_man;
            mParticleID = R.drawable.spark;
            mCameraSource.release();
            createCameraSource();
            startCameraSource();
        }
    }

    public void toggleBlackWidow(View view) {
        if (selectedHero != Superhero.BLACKWIDOW) {
            selectedHero = Superhero.BLACKWIDOW;
            mTextView.setText("Tilt your head");
            mFaceMaskID = R.drawable.black_widow_hair;
            mParticleID = R.drawable.black_widow_spider;
            mCameraSource.release();
            createCameraSource();
            startCameraSource();

        }
    }

    public void toggleHulk(View view) {
        if (selectedHero != Superhero.HULK) {
            selectedHero = Superhero.HULK;
            mTextView.setText("Open your mouth");
            mFaceMaskID = R.drawable.hulk_mask;
            mParticleID = R.drawable.hulk_fist;
            mCameraSource.release();
            createCameraSource();
            startCameraSource();

        }
    }

    public void updateParticleViewPosition (final int x, final int y, final float width, final float height) {

        //DisplayMetrics metrics = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(metrics);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mParticleView.setX(x-width*2);
                mParticleView.setY(y+100);
                Log.d(TAG,"Update view! x:" + (x) + " y: " + (y) + " w: " +width+ " h: " +height);
                //change size
                ViewGroup.LayoutParams params = mParticleView.getLayoutParams();
                params.width = (int) width*2;
                params.height = (int) height*2;
                mParticleView.setLayoutParams(params);
            }
        });


    }

    public void shootParticlesOnSmile() {
        mTextView.setText("");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                new ParticleSystem(mActivity, 100, mParticleID, 3000)
                        .setSpeedRange(0.2f, 0.5f)
                        .oneShot(mParticleView, 100);
            }
        });
    }


    private void shootParticlesOnEyesClosed() {
        mTextView.setText("Good job");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                new ParticleSystem(mActivity, 100, mParticleID, 3000)
                        .setSpeedRange(0.2f, 0.5f)
                        .oneShot(mParticleView, 100);
            }
        });
    }

    public void shootParticlesOnClick(View view) {
        Log.d(TAG,"Shooting particles!");
        new ParticleSystem(this, 100, mParticleID, 1000)
                .setSpeedRange(0.2f, 0.5f)
                .oneShot(mParticleView, 100);
    }
    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

//        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
//                Snackbar.LENGTH_INDEFINITE)
//                .setAction(R.string.ok, listener)
//                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            //TODO:reactivate
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);

            dlg.show();

        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }




    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay, mFaceMaskID);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        private boolean mSeriousSoundPlayed = false;
        private boolean mBatmanSoundPlayed = false;
        private boolean mIronManSoundPlayed = false;
        AudioManager mAudioManager;
        private boolean mHulkSoundPlayed = false;
        private MediaPlayer mHulkMediaPlayer;
        private MediaPlayer mIronManMediaPlayer;
        private MediaPlayer mBatmanMediaPlayer;
        private MediaPlayer mJokerMediaPlayer;

        GraphicFaceTracker(GraphicOverlay overlay, int faceMaskID) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, faceMaskID);
            mAudioManager = (AudioManager)mPreview.getContext().getSystemService(Context.AUDIO_SERVICE);
            mHulkMediaPlayer = MediaPlayer.create(mPreview.getContext(), R.raw.hulk_roar);
            mIronManMediaPlayer = MediaPlayer.create(mPreview.getContext(), R.raw.iron_man_repulsor);
            mJokerMediaPlayer = MediaPlayer.create(mPreview.getContext(), R.raw.serious);
            mBatmanMediaPlayer = MediaPlayer.create(mPreview.getContext(), R.raw.batman);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
            float smilingProbability = face.getIsSmilingProbability();

            updateParticleViewPosition((int) mFaceGraphic.translateX(face.getPosition().x),
                    (int) mFaceGraphic.translateY((int) face.getPosition().y),
                    face.getWidth(), face.getHeight());

            if (selectedHero == Superhero.BATMAN) {
                if (smilingProbability < 0.6f && smilingProbability > 0.1) {
                    Log.d(TAG, "Smiling! probability: " + String.valueOf(face.getIsSmilingProbability()));
                    //avoid updating on every frame
                    if (mTextView.getText() != "Why so serious?") {
                        mTextView.setText("Why so serious?");
                    }
                    if (mAudioManager != null && !mAudioManager.isMusicActive() && !mSeriousSoundPlayed) {
                        Log.d(TAG, "ACTIVATED!!!");
                        mSeriousSoundPlayed = true;

                        mJokerMediaPlayer.start();

                    }
                } else if (smilingProbability > 0.6f) {

                    if (mAudioManager != null && !mAudioManager.isMusicActive() && !mBatmanSoundPlayed) {
                        Log.d(TAG, "ACTIVATED!!!");
                        shootParticlesOnSmile();
                        mBatmanSoundPlayed = true;
                        mBatmanMediaPlayer.start();


                    }
                }
            } else if (selectedHero == Superhero.BLACKWIDOW) {
                if (smilingProbability > 0.6f && !mParticlesEmitting) {
                    mParticlesEmitting = true;
                }
            } else if (selectedHero == Superhero.IRONMAN) {

                if(face.getIsLeftEyeOpenProbability() < 0.4f && face.getIsLeftEyeOpenProbability() > 0.0f) {
                    if (mAudioManager != null && !mAudioManager.isMusicActive() && !mIronManSoundPlayed) {
                        Log.d(TAG, "ACTIVATED!!!");
                        mIronManSoundPlayed = true;
                        mIronManMediaPlayer.start();
                        shootParticlesOnEyesClosed();
                    }
                } else {
                    mIronManSoundPlayed = false;
                    if (mTextView.getText() != "Close your left eye") {
                        mTextView.setText("Close your left eye");
                    }
                }
            } else if ( selectedHero == Superhero.HULK) {
                if (mAudioManager != null && !mAudioManager.isMusicActive() && mFaceGraphic.isMouthOpen()
                        && !mHulkSoundPlayed) {
                    mHulkSoundPlayed = true;

                    mHulkMediaPlayer.start();
                    shootParticlesOnSmile();
                } else {
                    mHulkSoundPlayed = false;
                }
            }

        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }



}