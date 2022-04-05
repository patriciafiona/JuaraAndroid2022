package com.patriciafiona.tentangku.ui.main.weight.weightHome

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.patriciafiona.tentangku.R
import com.patriciafiona.tentangku.bottom_sheet.OnBottomSheetCallbacks
import com.patriciafiona.tentangku.data.source.local.entity.Weight
import com.patriciafiona.tentangku.databinding.FragmentWeightHomeBinding
import com.patriciafiona.tentangku.factory.ViewModelFactory
import com.patriciafiona.tentangku.ui.main.notes.NoteAdapter
import com.patriciafiona.tentangku.ui.main.weight.WeightActivity
import com.patriciafiona.tentangku.ui.main.weight.WeightAdapter
import com.patriciafiona.tentangku.ui.main.weight.WeightViewModel
import com.patriciafiona.tentangku.ui.main.weight.addUpdate.WeightAddUpdateActivity
import java.util.*
import kotlin.collections.ArrayList


class WeightHomeFragment : BottomSheetDialogFragment(), OnBottomSheetCallbacks {

    private var _binding: FragmentWeightHomeBinding? = null
    private val binding get() = _binding as FragmentWeightHomeBinding
    private var currentState: Int = BottomSheetBehavior.STATE_HALF_EXPANDED

    private var sortedFiveWeightList = ArrayList<Weight>()
    private lateinit var sortedWeightListDesc: List<Weight>
    private var listDate =  ArrayList<String>()

    private lateinit var adapter: WeightAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //set binding
        _binding = FragmentWeightHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        //set bottomSheet Callbacks
        (activity as WeightActivity).setOnBottomSheetCallbacks(this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkWeightAvailability(false)

        with(binding){
            indicatorImage.setOnClickListener {
                if (currentState == BottomSheetBehavior.STATE_EXPANDED) {
                    (activity as WeightActivity).closeBottomSheet()
                } else if (currentState == BottomSheetBehavior.STATE_HALF_EXPANDED){
                    (activity as WeightActivity).openBottomSheet()
                }
            }

            //Prepare chart
            initLineChart()

            val weightViewModel = obtainViewModel(requireActivity() as AppCompatActivity)
            weightViewModel.getAllWeight().observe(requireActivity()) { weightList ->
                if (weightList != null  && weightList.isNotEmpty()) {
                    sortedWeightListDesc = weightList.sortedByDescending { weight -> weight.date }

                    //clean arraylist
                    sortedFiveWeightList = ArrayList()
                    listDate = ArrayList()

                    for (i in 0..4){
                        sortedFiveWeightList.add(sortedWeightListDesc[i])
                    }

                    sortedFiveWeightList = ArrayList(sortedFiveWeightList.sortedBy{ weight -> weight.date })

                    for (dt in sortedFiveWeightList){
                        listDate.add(dt.date.toString())
                    }
                    setDataToLineChart()

                    //set to RV
                    adapter.setListWeight(sortedWeightListDesc)
                    checkWeightAvailability(true)
                }
            }
            //End of prepare chart

            adapter = WeightAdapter()
            rvWeights.layoutManager = LinearLayoutManager(requireActivity())
            rvWeights.setHasFixedSize(true)
            rvWeights.adapter = adapter

            btnAddWeight.setOnClickListener {
                val intent = Intent(requireActivity(), WeightAddUpdateActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun checkWeightAvailability(status: Boolean){
        with(binding){
            rvWeights.isVisible = status
            progressBar.isVisible = !status
        }
    }

    private fun setDataToLineChart() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()
        for (i in sortedFiveWeightList.indices) {
            val score = sortedFiveWeightList[i]
            entries.add(Entry(i.toFloat(), score.value?.toFloat() ?: 0f))
        }

        val lineDataSet = LineDataSet(entries, "")

        val data = LineData(lineDataSet)
        binding.weightProgressChart.data = data
        binding.weightProgressChart.invalidate()
    }

    private fun initLineChart() {
        with(binding) {
            val xAxis: XAxis = weightProgressChart.xAxis

            //remove right y-axis
            weightProgressChart.axisRight.isEnabled = false

            //remove legend
            weightProgressChart.legend.isEnabled = false

            //remove description label
            weightProgressChart.description.isEnabled = false

            //add animation
            weightProgressChart.animateX(1000, Easing.EaseInSine)

            // to draw label on xAxis
            val formatter: ValueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase): String {
                    return listDate[value.toInt()]
                }
            }

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = formatter
            xAxis.setDrawLabels(true)
            xAxis.granularity = 1f
            xAxis.labelRotationAngle = +90f
        }

    }

    private fun obtainViewModel(activity: AppCompatActivity): WeightViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory).get(WeightViewModel::class.java)
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        currentState = newState
        when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                binding.indicatorImage.setImageResource(R.drawable.ic_baseline_expand_more_eunry)
            }
            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                binding.indicatorImage.setImageResource(R.drawable.ic_baseline_expand_less_eunry)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }
}