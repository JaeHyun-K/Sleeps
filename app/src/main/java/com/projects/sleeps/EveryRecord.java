package com.projects.sleeps;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.JsonReader;
import com.projects.sleeps.MainActivity;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.projects.sleeps.RecordService;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EveryRecord extends JobService {
    MainActivity main=new MainActivity();
    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference=firebaseDatabase.getReference();
    //should check what service it will use!
    JobScheduler jobScheduler=(JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
    JobInfo jobInfo=new JobInfo.Builder(1, new ComponentName(this, EveryRecord.class))
            /*Do once a day*/
            .setPeriodic(TimeUnit.DAYS.toMillis(1))
            .build();
    RecordService service=new RecordService();
    String user=main.name;

    @Override
    public void onCreate() {
        super.onCreate();
        jobScheduler.schedule(jobInfo);
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        service.writeall();
        /*initialize JSON object about sleep, activity, sensor*/
        service.sensorObject=new JSONObject();
        service.geoObject=new JSONObject();
        service.activityObject=new JSONObject();
        service.sleepObject=new JSONObject();
        /*TODO: Send this JSONobject to server: service.JsonObject or file from internal storage*/
        databaseReference=firebaseDatabase.getReference().child(user);
        databaseReference.setValue(service.jsonObject);

        /*after sending it, initialize: wait for 1 hour to finish sending*/
        try {
            wait(TimeUnit.HOURS.toMillis(1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.jsonObject=new JSONObject();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
