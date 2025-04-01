package utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import lk.nd.rimberio.R;

public class ContactShopUtils {

    private static final int REQUEST_CAL_PERMISSION = 1;
    private Context context;

    public ContactShopUtils(Context context) {
        this.context = context;
    }

    public void contactShop(View view){
        Dialog dialog = new Dialog(view.getContext());
        dialog.setContentView(R.layout.contact_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        ImageView callImg = dialog.findViewById(R.id.callImg);
        ImageView messageImg = dialog.findViewById(R.id.messageImg);

        callImg.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(Intent.ACTION_CALL);
                Uri uri = Uri.parse("tel: 0718357354");
                i.setData(uri);
                ContextCompat.startActivity(view.getContext(), i, null);
                dialog.dismiss();
            }else{
                if (context instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CAL_PERMISSION);
                }
            }

        });

        messageImg.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SENDTO);
            Uri uri = Uri.parse("smsto: 0718367354");
            i.setData(uri);
            ContextCompat.startActivity(view.getContext(), i, null);
            dialog.dismiss();
        });

        dialog.show();
    }

}
