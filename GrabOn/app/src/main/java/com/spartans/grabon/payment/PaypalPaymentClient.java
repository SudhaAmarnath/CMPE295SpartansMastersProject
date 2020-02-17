package com.spartans.grabon.payment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.spartans.grabon.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;


public class PaypalPaymentClient extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypal_payment_client);

        Button paymentButton;

        paymentButton = findViewById(R.id.PaymentButton);

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paypalPayment();
            }
        });


    }

    private int PAYPAL_REQUEST_CODE = 1;

    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PaypalPaymentConfig.PAYPAL_CLIENT_ID);


    private void paypalPayment() {

        PayPalPayment payment = new PayPalPayment(new BigDecimal(1.00), "USD", "pay",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent i = new Intent(this, PaymentActivity.class);

        i.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        i.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(i, PAYPAL_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        {
                            JSONObject jsonObj = new JSONObject(confirm.toJSONObject().toString());

                            String paymentResponse = jsonObj.getJSONObject("response").getString("state");
//                            paymentId = confirm.toJSONObject().getJSONObject("response").getString("id");
                            // verifyPaymentOnServer(payment_id,confirm);
                            //displayResultText("PaymentConfirmation info received from PayPal");

                            if (paymentResponse.equals("approved")) {

//                                Toast.makeText(getContext(),"Payment successful",Toast.LENGTH_LONG).show();
//                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//                                assert currentUser != null;
//                                String uid = currentUser.getUid();
//                                HashMap<String,Boolean> m=new HashMap<>();
//                                m.put("customerPaid",true);
//                                firestore.collection("Users")
//                                        .document(uid)
//                                        .set(m, SetOptions.merge());

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Payment Unsuccessful", Toast.LENGTH_LONG).show();

            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            }
        }
    }
    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

}
