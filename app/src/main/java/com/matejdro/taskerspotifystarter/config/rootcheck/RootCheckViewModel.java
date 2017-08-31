package com.matejdro.taskerspotifystarter.config.rootcheck;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import com.matejdro.taskerspotifystarter.util.Resource;
import com.matejdro.taskerspotifystarter.util.RootUtils;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class RootCheckViewModel extends ViewModel {
    private CompositeDisposable disposable;
    private MutableLiveData<Resource<Boolean>> rootStatusLivedata = new MutableLiveData<>();

    public RootCheckViewModel() {
        this.disposable = new CompositeDisposable();

        checkForRoot();
    }

    public LiveData<Resource<Boolean>> getRootStatusLivedata() {
        return rootStatusLivedata;
    }

    public void checkForRoot() {
        rootStatusLivedata.setValue(Resource.loading(null));

        RootUtils
                .checkRoot()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rootStatusObserver);
    }

    private SingleObserver<Boolean> rootStatusObserver = new SingleObserver<Boolean>() {
        @Override
        public void onSubscribe(Disposable d) {
            disposable.add(d);
        }

        @Override
        public void onSuccess(Boolean rootStatus) {
            rootStatusLivedata.setValue(Resource.success(rootStatus));
        }

        @Override
        public void onError(Throwable e) {
            rootStatusLivedata.setValue(Resource.error((Exception) e));
        }
    };

    @Override
    protected void onCleared() {
        disposable.dispose();

        super.onCleared();
    }
}
