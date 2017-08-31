package com.matejdro.taskerspotifystarter.config.userlibrary;

import android.app.Application;
import android.arch.lifecycle.*;
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyListType;
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyPlaybackItem;
import com.matejdro.taskerspotifystarter.spotifydata.providers.SpotifyListProvider;
import com.matejdro.taskerspotifystarter.util.Resource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import java.util.List;

public class LibraryListViewModel extends AndroidViewModel {
    private SpotifyListProvider<?> listProvider;

    private MutableLiveData<Resource<List<SpotifyPlaybackItem>>> itemsLiveData;
    private CompositeDisposable disposable;

    private boolean reachedEnd = false;
    private boolean loading = false;
    private int prevItemCount = 0;

    public LibraryListViewModel(SpotifyListType listType, Application application) {
        super(application);

        disposable = new CompositeDisposable();

        listProvider = SpotifyListProvider.create(application, listType);

        listProvider
                .getListObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ItemsObserver());

        itemsLiveData = new MutableLiveData<>();

        listProvider.requestListStart();
        loading = true;
    }

    public void loadNextPage() {
        if (reachedEnd || loading) {
            return;
        }

        loading = true;
        listProvider.requestNextPage();
    }

    public LiveData<Resource<List<SpotifyPlaybackItem>>> getItemsLiveData() {
        return itemsLiveData;
    }

    private class ItemsObserver implements Observer<Resource<List<SpotifyPlaybackItem>>> {

        @Override
        public void onSubscribe(Disposable d) {
            disposable.add(d);
        }

        @Override
        public void onNext(Resource<List<SpotifyPlaybackItem>> listResource) {
            if (listResource.status == Resource.Status.SUCCESS) {
                loading = false;
                assert listResource.data != null;
                reachedEnd = prevItemCount == listResource.data.size();
                prevItemCount = listResource.data.size();
            }

            itemsLiveData.setValue(listResource);
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onComplete() {

        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposable.dispose();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private SpotifyListType spotifyListType;
        private Application application;

        public Factory(SpotifyListType spotifyListType, Application application) {
            this.spotifyListType = spotifyListType;
            this.application = application;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new LibraryListViewModel(spotifyListType, application);
        }
    }
}
