package com.spartans.grabon.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.spartans.grabon.R;
import com.spartans.grabon.payment.PaypalPaymentClient;

public class Cart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Button cartProceedForPayment;

        cartProceedForPayment = findViewById(R.id.CartProceedForPayment);

        cartProceedForPayment.setOnClickListener(new View.OnClickListener() {
        @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), PaypalPaymentClient.class));
                finish();
            }
            }
        );

    }
}
