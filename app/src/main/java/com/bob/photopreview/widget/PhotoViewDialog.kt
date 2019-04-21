package com.wuhui.weface.dialog.PhotoPreview

import android.animation.*
import android.content.Context
import android.graphics.Color
import android.graphics.Path
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.bob.photopreview.R
import com.bob.photopreview.util.BitmapUtils
import com.bob.tongchuang.util.StatusBarUtil
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class PhotoViewDialog : DialogFragment() {

    var onPhotoPreviewListener: OnPhotoPreviewListener? = null
    var targetView: ImageView? = null
    lateinit var animationView: ImageView
    lateinit var rootView: ViewGroup
    lateinit var viewPager: ViewPager
    lateinit var photoAdapter: PhotoPagerAdapter
    var position = 0

    companion object {
        fun newInstance(urls: ArrayList<String>, position: Int): PhotoViewDialog {
            return PhotoViewDialog().apply {
                val args = Bundle()
                args.putStringArrayList("urls", urls)
                args.putInt("position", position)
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullcreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val urls = arguments!!.getStringArrayList("urls")
        position = arguments!!.getInt("position")

        rootView = FrameLayout(context)
        rootView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        animationView = ImageView(context)
        animationView.setImageBitmap(BitmapUtils.drawableToBitmap(targetView!!.drawable))
        viewPager = ViewPager(context!!)
        viewPager.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        viewPager.setBackgroundColor(Color.TRANSPARENT)

        photoAdapter = PhotoPagerAdapter(urls!!).apply {
            rootView = this@PhotoViewDialog.rootView
            animationView = this@PhotoViewDialog.animationView
        }
        rootView.addView(animationView)
        rootView.addView(viewPager)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(position: Int) {
                targetView = onPhotoPreviewListener?.onPhotoPreview(position) as ImageView
            }
        })
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (targetView != null) {
            val location = IntArray(2)
            targetView!!.getLocationOnScreen(location)
            val w = targetView!!.measuredWidth
            val h = targetView!!.measuredHeight
            animationView.layoutParams = FrameLayout.LayoutParams(w, h).apply {
                leftMargin = location[0]
                topMargin = location[1] - StatusBarUtil.getStatusBarHeight(context!!)
            }
            rootView.post {
                val animator = ObjectAnimator.ofObject(LayoutParamsEvaluator(), animationView.layoutParams, FrameLayout.LayoutParams(rootView.measuredWidth, rootView.measuredHeight))
                animator.start()
                animator.addUpdateListener {
                    val params = it.animatedValue as FrameLayout.LayoutParams
                    animationView.layoutParams = params
                    rootView.setBackgroundColor(Color.argb((255 * it.animatedFraction).toInt(), 0, 0, 0))
                    if (it.animatedFraction == 1.0f) {
                        viewPager.adapter = photoAdapter
                        viewPager.currentItem = position
                        animationView.postDelayed({
                            animationView.visibility = View.INVISIBLE
                        }, 200)
                    }
                }
            }
        }
    }

    fun previewCancel(photoContainer: PhotoPagerAdapter.PhotoContainer) {

        animationView.apply {
            setImageBitmap(BitmapUtils.drawableToBitmap(photoContainer.photoView.drawable))
            visibility = View.VISIBLE
            scaleType = targetView!!.scaleType
        }
        val w = rootView.measuredWidth * photoContainer.scaleX
        val h = rootView.measuredHeight * photoContainer.scaleY
        val marginTop = rootView.top + (rootView.bottom - rootView.top) / 2 + photoContainer.translationY - (rootView.measuredHeight * photoContainer.scaleY / 2)
        val marginLeft = rootView.left + (rootView.right - rootView.left) / 2 + photoContainer.translationX - (rootView.measuredWidth * photoContainer.scaleX / 2)
        val startParams = FrameLayout.LayoutParams(w.toInt(), h.toInt()).apply {
            topMargin = marginTop.toInt()
            leftMargin = marginLeft.toInt()
        }
        animationView.layoutParams = startParams
        viewPager.visibility = View.INVISIBLE

        val location = IntArray(2)
        targetView!!.getLocationOnScreen(location)
        val endParams = FrameLayout.LayoutParams(targetView!!.measuredWidth, targetView!!.measuredHeight).apply {
            leftMargin = location[0]
            topMargin = location[1] - StatusBarUtil.getStatusBarHeight(context!!)
        }
        val animator = ObjectAnimator.ofObject(LayoutParamsEvaluator(), startParams, endParams)
        animator.start()
        animator.addUpdateListener {
            val params = it.animatedValue as FrameLayout.LayoutParams
            animationView.layoutParams = params
            rootView.alpha = 1 - it.animatedFraction
            if (it.animatedFraction == 1.0f) {
                dismiss()
            }
        }
    }

    inner class PhotoPagerAdapter(var urls: ArrayList<String>) : PagerAdapter() {

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val photoContainer = PhotoContainer(container.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            Glide.with(container.context).load(urls[position]).into(photoContainer.photoView)
            container.addView(photoContainer)
            return photoContainer
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View?)
        }

        override fun getCount(): Int {
            return urls.size
        }

        inner class PhotoContainer(context: Context?) : FrameLayout(context) {
            var photoView: PhotoView = PhotoView(context)
            private var preY: Float = 0f
            private var totalY: Float = 0f
            private var preX: Float = 0f
            private var totalX: Float = 0f
            private var heightPixels = resources.displayMetrics.heightPixels

            init {
                addView(photoView.apply {
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                })
            }

            override fun onTouchEvent(event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        val currentY = event.rawY
                        val currentX = event.rawX
                        val dy = currentY - preY
                        val dx = currentX - preX
                        totalY += dy
                        totalX += dx
                        var scale = (heightPixels - totalY) / heightPixels
                        if (scale > 1) {
                            scale = 1f
                        }
                        scaleY = scale
                        scaleX = scale
                        translationY = totalY
                        translationX = totalX

                        this@PhotoViewDialog.rootView.setBackgroundColor(Color.argb((255 * scale).toInt(), 0, 0, 0))
                        preY = currentY
                        preX = currentX
                    }
                    MotionEvent.ACTION_UP -> {
                        if (scaleX <= 0.65f) {
                            previewCancel(this)
                        } else {
                            val path1 = Path()
                            path1.moveTo(scaleX, scaleY)
                            path1.lineTo(1f, 1f)
                            val scale = ObjectAnimator.ofFloat(this@PhotoContainer, "scaleX", "scaleY", path1)
                            val path2 = Path()
                            path2.moveTo(translationX, translationY)
                            path2.lineTo(0f, 0f)
                            val translation = ObjectAnimator.ofFloat(this@PhotoContainer, "translationX", "translationY", path2)
                            val set = AnimatorSet()
                            set.playTogether(scale, translation)
                            set.start()
                            set.addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {
                                    this@PhotoViewDialog.rootView.setBackgroundColor(Color.argb(255, 0, 0, 0))
                                }
                            })
                        }
                    }
                }
                return super.onTouchEvent(event)
            }

            override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
                when (ev.action) {
                    MotionEvent.ACTION_DOWN -> {
                        preY = ev.rawY
                        preX = ev.rawX
                        totalX = 0f
                        totalY = 0f
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val currentY = ev.rawY
                        if (currentY > preY && photoView.scale <= 1f) {
                            if (animationView.visibility == View.VISIBLE) {
                                animationView.visibility = View.INVISIBLE
                            }
                            return true
                        }
                    }
                }
                return super.onInterceptTouchEvent(ev)
            }

        }

    }

    interface OnPhotoPreviewListener {
        fun onPhotoPreview(index: Int): View?
    }

}


class LayoutParamsEvaluator : TypeEvaluator<FrameLayout.LayoutParams> {
    override fun evaluate(fraction: Float, start: FrameLayout.LayoutParams, end: FrameLayout.LayoutParams): FrameLayout.LayoutParams {
        val width = start.width + (end.width - start.width) * fraction
        val height = start.height + (end.height - start.height) * fraction
        val top = (start as ViewGroup.MarginLayoutParams).topMargin + ((end as ViewGroup.MarginLayoutParams).topMargin - start.topMargin) * fraction
        val left = start.leftMargin + (end.leftMargin - start.leftMargin) * fraction
        return FrameLayout.LayoutParams(width.toInt(), height.toInt()).apply {
            topMargin = top.toInt()
            leftMargin = left.toInt()
        }
    }
}

