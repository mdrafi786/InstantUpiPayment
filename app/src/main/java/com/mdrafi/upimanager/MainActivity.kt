package com.mdrafi.upimanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mdrafi.instantupipayment.InstantUpiPayment
import com.mdrafi.instantupipayment.model.PaymentApp
import com.mdrafi.instantupipayment.model.TransactionStatus
import com.mdrafi.upimanager.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var instantUpiPayment: InstantUpiPayment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        val transactionId = "TID" + System.currentTimeMillis()
        binding.fieldTransactionId.setText(transactionId)
        binding.fieldTransactionRefId.setText(transactionId)
        // Setup click listener for Pay button
        binding.buttonPay.setOnClickListener {
            pay()
        }
    }

    private fun pay() {
        val paymentApp = when (binding.radioAppChoice.checkedRadioButtonId) {
            R.id.app_default -> PaymentApp.ALL
            R.id.app_amazonpay -> PaymentApp.AMAZON_PAY
            R.id.app_bhim_upi -> PaymentApp.BHIM_UPI
            R.id.app_google_pay -> PaymentApp.GOOGLE_PAY
            R.id.app_phonepe -> PaymentApp.PHONE_PE
            R.id.app_paytm -> PaymentApp.PAYTM
            else -> throw IllegalStateException("Unexpected value: " + binding.radioAppChoice.id)
        }

        try {
            // START PAYMENT INITIALIZATION
            instantUpiPayment = InstantUpiPayment(this) {
                this.paymentApp = paymentApp
                this.payeeVpa = binding.fieldVpa.text.toString()
                this.payeeName = binding.fieldName.text.toString()
                this.transactionId = binding.fieldTransactionId.text.toString()
                this.transactionRefId = binding.fieldTransactionRefId.text.toString()
                this.payeeMerchantCode = binding.fieldPayeeMerchantCode.text.toString()
                this.description = binding.fieldDescription.text.toString()
                this.amount = binding.fieldAmount.text.toString()
            }
            // END INITIALIZATION

            // Register Listener for Events
            instantUpiPayment.addPaymentStatusListener({ transactionDetails ->
                Log.d("TransactionDetails", transactionDetails.toString())
                binding.textViewStatus.text = transactionDetails.toString()

                when (transactionDetails.transactionStatus) {
                    TransactionStatus.SUCCESS -> onTransactionSuccess()
                    TransactionStatus.FAILED -> onTransactionFailed()
                    TransactionStatus.SUBMITTED -> onTransactionSubmitted()
                }
            }, {
                showToast(it)
                binding.imageView.setImageResource(R.drawable.ic_failed)
            })

            // Start payment / transaction
            instantUpiPayment.startPayment(activityResult)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error: ${e.message}")
        }
    }

    private fun onTransactionSuccess() {
        // Payment Success
        showToast("Success")
        binding.imageView.setImageResource(R.drawable.ic_success)
    }

    private fun onTransactionSubmitted() {
        // Payment Pending
        showToast("Pending | Submitted")
        binding.imageView.setImageResource(R.drawable.ic_success)
    }

    private fun onTransactionFailed() {
        // Payment Failed
        showToast("Failed")
        binding.imageView.setImageResource(R.drawable.ic_failed)
    }

    var activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        instantUpiPayment.onActivityResult(result)
    }

}