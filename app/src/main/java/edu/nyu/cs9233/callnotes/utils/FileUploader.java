package edu.nyu.cs9233.callnotes.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

import edu.nyu.cs9233.callnotes.R;

public class FileUploader {

    private final AmazonS3Client amazonS3Client;
    private final TransferUtility transferUtility;

    private Context context;

    public FileUploader(Context context) {
        this.context = context;

        amazonS3Client = new AmazonS3Client(AWSMobileClient.getInstance().getCredentials());
        transferUtility = TransferUtility.builder()
                .s3Client(amazonS3Client)
                .context(context)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
    }

    public void uploadFromInternalStorage(String filePathInInternalStorage, Listeners.PersistObjectListener listener) {
        upload(new File(ContextCompat.getDataDir(context), "files/"+filePathInInternalStorage), listener);
    }

    public void upload(File file, final Listeners.PersistObjectListener listener) {
        if (file == null || !file.exists()) {
            Debug.log("NO FILE FOUND TO UPLOAD");
            return;
        }
        TransferObserver observer = transferUtility.upload(context.getString(R.string.s3BucketName), file.getName(), file);

        if (listener == null) {
            return;
        }

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Debug.log("Transfer state changed", state);
                if (state.equals(TransferState.COMPLETED)) {
                    listener.onSuccess();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Debug.log("Transfer progress change ", id, bytesCurrent, bytesTotal);
            }

            @Override
            public void onError(int id, Exception ex) {
                Debug.log("Upload Error: ", id);
                ex.printStackTrace();
                listener.onFailure(ex);
            }
        });
    }

}
