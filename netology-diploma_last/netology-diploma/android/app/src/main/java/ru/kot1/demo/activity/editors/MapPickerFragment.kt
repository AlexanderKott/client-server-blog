package ru.kot1.demo.activity.editors

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.kot1.demo.R
import ru.kot1.demo.databinding.FragmentMapPickerBinding


class MapPickerFragment : InputListener,
     Fragment()  {
    private var _binding: FragmentMapPickerBinding? = null
     private var point : Point = Point()

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapPickerBinding.inflate(inflater, container, false)
        return _binding!!.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
     //   MapKitFactory.initialize(requireContext())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_new_record, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home ->{
                activity?.onBackPressed()
                true
            }

            R.id.save -> {
                if (point.latitude == 0.0 && point.longitude == 0.0 ){
                    Toast.makeText(requireContext(), R.string.pick_point_on_map,
                        Toast.LENGTH_LONG).show()
                    return false
                }

                val myIntent = Intent()
                myIntent.putExtra("lat", point.latitude.toFloat())
                myIntent.putExtra("long", point.longitude.toFloat())
                 requireActivity().setResult(Activity.RESULT_OK, myIntent);

                activity?.finish()
                true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.choose_place)

        val lat = activity?.intent?.getFloatExtra("lat", 0f) ?: 0f
        val long = activity?.intent?.getFloatExtra("long", 0f) ?: 0f

        if (lat != 0F && long != 0F){
       _binding?.mapview?.map?.move(
            CameraPosition(Point(lat.toDouble(), long.toDouble()), 3.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1F),
            null)

                val mapObjects =  _binding?.mapview?.map?.mapObjects
                mapObjects?.let{
                    val mark = it.addPlacemark(Point(lat.toDouble(), long.toDouble()))
                    mark.setIcon(ImageProvider.fromBitmap(getBitmapFromVectorDrawable(R.drawable.geom)))
                    point = Point(lat.toDouble(), long.toDouble())
                }

        }

        _binding?.mapview?.map?.addInputListener(this)
    }


    fun Map.moveAnimated(point: Point, zoom: Float = 10F) {
        move(cameraPosition(point, zoom), Animation(Animation.Type.SMOOTH, 1F), null)
    }

    private fun cameraPosition(point: Point, zoom: Float): CameraPosition =
        CameraPosition(point, zoom, 0F, 0F)


    override fun onStop() {
        super.onStop()
        _binding?.mapview?.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onStart() {
        super.onStart()
        _binding?.mapview?.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onMapTap(map: com.yandex.mapkit.map.Map, pointM: Point) {
        map.mapObjects.clear()
        val mapObjects =  map.mapObjects.addCollection()
        mapObjects.let{
            val mark: PlacemarkMapObject = mapObjects.addPlacemark(pointM)
            mark.opacity = 0.5f
            mark.setIcon(ImageProvider.fromBitmap(getBitmapFromVectorDrawable(R.drawable.geom)))
            mark.isDraggable = true
            point = Point(pointM.latitude, pointM.longitude)

        }
    }

    override fun onMapLongTap(map: com.yandex.mapkit.map.Map, p1: Point) {
        map.moveAnimated(p1, 5F)
    }


    fun  getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
        var drawable = ContextCompat.getDrawable(requireContext(), drawableId) ?: return null

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable).mutate()
        }

        val bitmap = Bitmap.createBitmap(
            110,
            110,
            Bitmap.Config.ARGB_8888) ?: return null

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }


}


