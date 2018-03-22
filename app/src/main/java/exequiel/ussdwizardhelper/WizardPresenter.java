package exequiel.ussdwizardhelper;

import android.support.annotation.Nullable;
import android.util.Log;

import exequiel.ussdwizardhelper.http.data.response.Nwspersonans;
import exequiel.ussdwizardhelper.http.data.response.UserResponseEnvelop;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by egonzalez on 08/03/18.
 */

public class WizardPresenter implements MVPWizard.Presenter {


    @Nullable
    private MVPWizard.View mView;
    private final MVPWizard.Model model;
    private String TAG = WizardPresenter.class.getCanonicalName();

    public WizardPresenter(MVPWizard.Model model){
        this.model = model;
    }

    @Override
    public void setView(MVPWizard.View view) {
        mView = view;
    }

    @Override
    public void fabClicked() {
        if (!mView.checkInternet()){
            mView.showMessage(R.string.internet_error);
        }else if (!mView.checkSIM()){
            mView.showMessage(R.string.sim_error);
        }else if (!mView.checkCall()){
            mView.showMessage(R.string.call_error);
        }else if (!mView.checkAccesibility()){
            mView.showMessage(R.string.accesibility_error);
        }else {

            /**
             * Improve these yet i need the ws
             */
                model.getUser()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<UserResponseEnvelop>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, e.toString());
                            }

                            @Override
                            public void onNext(UserResponseEnvelop user) {
                                String suceso = user.getBody().getExecuteResponse().getResultado2Ns().getSuceso();
                                if (suceso.equals(1)){
                                    Nwspersonans nwspersonans = user.getBody().getExecuteResponse().getNwspersonans();
                                    Log.d(TAG, nwspersonans.toString());
                                    String uId = nwspersonans.getNPer00NroDoc();
                                    String uDate = nwspersonans.getNPer00FecNac();
                                    model.saveUser(uId, uDate);
                                    mView.callUSSDService();
                                }else
                                {
                                    mView.showMessage(R.string.register_error);
                                }
                            }
                        });
        }
    }

    @Override
    public void changeState() {
        if (model.getSate().equals("succes")){
            mView.changeFab("succes");
            mView.changeText("succes");
        }
        if (model.getSate().equals("registered")){
            mView.changeFab("registered");
            mView.changeText("registered");
        }
    }


}
