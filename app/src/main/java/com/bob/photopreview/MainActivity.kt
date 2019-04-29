package com.bob.photopreview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.wuhui.weface.dialog.PhotoPreview.PhotoViewDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val list = arrayListOf("http://d.hiphotos.baidu.com/lvpics/w=1000/sign=e2347e78217f9e2f703519082f00eb24/730e0cf3d7ca7bcb49f90bb1b8096b63f724a8aa.jpg",
                "http://pic31.nipic.com/20130804/7487939_090818211000_2.jpg",
                "http://pic23.nipic.com/20120817/6837854_104558027324_2.jpg")
        val images = arrayOf(image1, image2, image3)
        for (index in 0 until images.size) {
            val options = RequestOptions().override(Target.SIZE_ORIGINAL)
            Glide.with(this).load(list[index]).apply(options).into(images[index])
            images[index].setOnClickListener {
                val dialog = PhotoViewDialog.newInstance(list, index)
                dialog.targetView = images[index]
                dialog.onPhotoPreviewListener = object : PhotoViewDialog.OnPhotoPreviewListener {
                    override fun onPhotoPreview(index: Int): View? {
                        return images[index]
                    }
                }
                dialog.show(supportFragmentManager, null)
            }
        }

    }
}
