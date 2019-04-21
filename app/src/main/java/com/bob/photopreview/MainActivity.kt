package com.bob.photopreview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.wuhui.weface.dialog.PhotoPreview.PhotoViewDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 4)
            adapter = ImageAdapter()
        }

    }

    class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
        private val list = arrayListOf("http://d.hiphotos.baidu.com/lvpics/w=1000/sign=e2347e78217f9e2f703519082f00eb24/730e0cf3d7ca7bcb49f90bb1b8096b63f724a8aa.jpg",
                "http://pic31.nipic.com/20130804/7487939_090818211000_2.jpg",
                "http://pic23.nipic.com/20120817/6837854_104558027324_2.jpg",
                "http://pic39.nipic.com/20140320/12795880_110914420143_2.jpg")

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val imageView = ImageView(parent.context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                layoutParams = RecyclerView.LayoutParams(200, 200)
            }
            return ViewHolder(imageView)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.apply {
                val url = list[position]
                val options = RequestOptions().override(Target.SIZE_ORIGINAL)
                Glide.with(context).load(url).apply(options).into(holder.itemView as ImageView)
                setOnClickListener {
                    val dialog = PhotoViewDialog.newInstance(list, position).apply {
                        targetView = holder.itemView
                        onPhotoPreviewListener = object : PhotoViewDialog.OnPhotoPreviewListener {
                            override fun onPhotoPreview(index: Int): View? {
                                return holder.itemView
                            }
                        }
                    }
                    val activity = context as AppCompatActivity
                    dialog.show(activity.supportFragmentManager, null)
                }

            }

        }

        class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
    }
}
