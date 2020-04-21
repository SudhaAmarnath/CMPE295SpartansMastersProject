package com.spartans.grabon.payment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.spartans.grabon.R;
import com.spartans.grabon.cart.Cart;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Author : Sudha Amarnath on 2020-02-16
 */
public class PaypalPaymentClient extends AppCompatActivity {

    private double grandtotal = 0;
    private String recepient = null;

    private int PAYPAL_REQUEST_CODE = 1;
    private int PAYPAL_TO_CART_SUCCESS_CODE = 1;
    private int PAYPAL_TO_CART_FAILURE_CODE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypal_payment_client);

        grandtotal = (Double) getIntent().getSerializableExtra("grandtotal");
        recepient = (String) getIntent().getSerializableExtra("recepient");

        TextView paymentAmount = findViewById(R.id.PaymentAmount);
        FancyButton paymentButton = findViewById(R.id.PaymentButton);
        FancyButton backToCart = findViewById(R.id.PaymentBackToCart);
        paymentAmount.setText("$"+String.format("%.2f",grandtotal));

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        backToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent r = new Intent(PaypalPaymentClient.this, Cart.class);
                r.putExtra("paymentid", "");
                setResult(PAYPAL_TO_CART_FAILURE_CODE, r);
                finish();
            }
        });

        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paypalPayment();
            }
        });

    }

    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PaypalPaymentConfig.PAYPAL_CLIENT_ID);

    private void paypalPayment() {

        PayPalPayment payment = new PayPalPayment(new BigDecimal(grandtotal), "USD", "pay",
                PayPalPayment.PAYMENT_INTENT_SALE);
        payment.payeeEmail(recepient);
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
                        JSONObject jsonObj = new JSONObject(confirm.toJSONObject().toString());
                        String paymentResponse = jsonObj.getJSONObject("response").getString("state");
                        if (paymentResponse.equals("approved")) {
                            String paymentId = confirm.toJSONObject().getJSONObject("response").getString("id");
                            Log.v("Paypal:","Payment successful with id:" + paymentId);
                            Toast.makeText(getApplicationContext(),"Payment successful",Toast.LENGTH_LONG).show();
                            Intent resultcart = new Intent(PaypalPaymentClient.this, Cart.class);
                            resultcart.putExtra("paymentid", paymentId);
                            setResult(PAYPAL_TO_CART_SUCCESS_CODE, resultcart);
                            finish();
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

    @Override
    public void onBackPressed() {
        Intent r = new Intent(PaypalPaymentClient.this, Cart.class);
        r.putExtra("paymentid", "");
        setResult(PAYPAL_TO_CART_FAILURE_CODE, r);
        finish();
    }

}
