package uteq.solutions.downloadmanagerkt

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {
    var downloadid: Long = 0
    lateinit var txt: TextView
    lateinit var adminPermisos: AdministradorPermisos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adminPermisos = AdministradorPermisos(this@MainActivity)

        val permisosSolicitados = ArrayList<String?>()
        permisosSolicitados.add(Manifest.permission.CAMERA)
        permisosSolicitados.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permisosSolicitados.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        permisosSolicitados.add(Manifest.permission.WRITE_CALENDAR)


        val permisosAprobados:ArrayList<String?>   = adminPermisos.getPermisosAprobados(permisosSolicitados)
        val listPermisosNOAprob:ArrayList<String?> = adminPermisos.getPermisosNoAprobados(permisosSolicitados)

        txt =  findViewById<TextView>(R.id.txt)
        txt.setText("Permisos Aprobados:\n" + permisosAprobados.toString()+"\n")

        adminPermisos.getPermission(listPermisosNOAprob)


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var resp:String  = adminPermisos.onRequestPermissionsResult(requestCode, permissions as Array<String>, grantResults)
        Toast.makeText(this.applicationContext, resp, Toast.LENGTH_LONG).show()
    }





    fun MostrarDescargas(view: View?) {
        val intent = Intent()
        intent.action = DownloadManager.ACTION_VIEW_DOWNLOADS
        startActivity(intent)
    }


    fun BajarDoc(view: View?) {

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val request =  DownloadManager.Request(Uri.parse("https://www.uteq.edu.ec/doc/investigacion/lineas_inv.pdf"))
                            .setDescription("Download PDF")
                            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                            .setTitle("Download Pdf")
                            .setAllowedOverMetered(true)
                            .setVisibleInDownloadsUi(true)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalFilesDir(this.applicationContext, Environment.DIRECTORY_DOWNLOADS,"downloadfile.pdf")



        try {
            downloadid = manager.enqueue(request)
            registerReceiver(MyBroadcastReceiver(downloadid), IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        } catch (e: Exception) {
            Toast.makeText(this.applicationContext, "Error: " + e.message, Toast.LENGTH_LONG).show()
        }
    }
}


class MyBroadcastReceiver(var downloadid: Long) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        if (id == downloadid)
            Toast.makeText(context,  "Download Done!!",Toast.LENGTH_LONG).show()
    }
}

class AdministradorPermisos(var context: Context) {

    fun getPermisosNoAprobados(listaPermisos: ArrayList<String?>): ArrayList<String?> {
        val list = ArrayList<String?>()
        for (permiso in listaPermisos) {
            if (Build.VERSION.SDK_INT >= 23)
                if (context.checkSelfPermission(permiso!!) != PackageManager.PERMISSION_GRANTED)
                    list.add(permiso)
        }
        return list
    }

    fun getPermisosAprobados(listaPermisos: ArrayList<String?>): ArrayList<String?> {
        val list = ArrayList<String?>()
        for (permiso in listaPermisos) {
            if (Build.VERSION.SDK_INT >= 23)
                if (context.checkSelfPermission(permiso!!) == PackageManager.PERMISSION_GRANTED)
                    list.add(permiso)
        }
        return list
    }

    fun getPermission(permisosSolicitados: ArrayList<String?>) {
        if(permisosSolicitados.size>0)
            if (Build.VERSION.SDK_INT >= 23)
                ActivityCompat.requestPermissions(context as Activity, permisosSolicitados.toArray(arrayOfNulls(permisosSolicitados.size)),1)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,grantResults: IntArray):String  {
        var s = ""
        if (requestCode == 1) {
            for (i in permissions.indices) {
                s+= if(grantResults[i] == PackageManager.PERMISSION_GRANTED)"Permitido: " else "Denegado: "
                s+=" " + permissions[i] + "\n"
            }
        }
        return s
    }

}