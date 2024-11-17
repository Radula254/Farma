package com.example.mkulifarm.ui.theme

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mkulifarm.R
import com.example.mkulifarm.data.MetricData
import com.example.mkulifarm.data.MetricsAnalysis.MetricsViewModel
import com.example.mkulifarm.data.MetricsAnalysis.MockMetricsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.google.firebase.FirebaseApp

class DataAnalysis : ComponentActivity() {

    private val metricsViewModel: MetricsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_MKuliFarm)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        setContent {

                FarmAnalysisScreen(viewModel = metricsViewModel)
            }

    }
    }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmAnalysisScreen(viewModel: MetricsViewModel) {
    val metrics by viewModel.metrics.collectAsState(initial = emptyList())

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Farm Data Analysis",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Weekly Trends",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (metrics.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Line Chart for Weekly Trends
                    WeeklyTrendChart(metrics = metrics)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Insights Section
                    InsightsSection(metrics = metrics)
                }
            }
        }
    }


@Composable
fun WeeklyTrendChart(metrics: List<MetricData>) {
    // Create data points for a LineChart
    val entries = metrics.mapIndexed { index, metric ->
        Entry(index.toFloat(), metric.value)
    }
    val dataSet = LineDataSet(entries, "Weekly Metrics").apply {
        color = Color.Green.toArgb()
        lineWidth = 2f
        setCircleColor(Color.Green.toArgb())
        circleRadius = 4f
        valueTextColor = Color.Gray.toArgb()
        valueTextSize = 10f
        mode = LineDataSet.Mode.CUBIC_BEZIER
    }

    val lineData = LineData(dataSet)

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            LineChart(context).apply {
                this.data = lineData
                this.description.isEnabled = false
                this.setDrawGridBackground(false)
                this.xAxis.apply {
                    granularity = 1f
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                }
                this.axisLeft.setDrawGridLines(false)
                this.axisRight.isEnabled = false
                this.invalidate() // Refresh the chart
            }
        }
    )
}

@Composable
fun InsightsSection(metrics: List<MetricData>) {
    val lightGreen = Color(0xFF8BC34A)

    // Column to hold the entire section
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Weekly Insights",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Use LazyRow to display cards side by side
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between cards
        ) {
            items(metrics) { metric ->
                Card(
                    modifier = Modifier
                        .width(120.dp) // Fixed width for each card
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = lightGreen)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        // Metric Name and Value
                        Text(
                            text = metric.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${metric.value} ${metric.unit}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // Displaying advice for each metric
                        val advice = getMetricAdvice(metric)
                        Spacer(modifier = Modifier.height(8.dp)) // Add space before advice
                        Text(
                            text = advice,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

// Function to provide advice based on the metric
fun getMetricAdvice(metric: MetricData): String {
    return when (metric.name) {
        "Fertilizer Use" -> "Recommended fertilizer usage is within optimal range."
        "Water Usage" -> "Water usage is slightly high. Consider reducing irrigation."
        "Soil Health" -> "Soil health is stable, but periodic testing is advised."
        else -> "No advice available for this metric."
    }
}


// Example function that uses ML to generate insights
fun getWeeklyInsights(metrics: List<MetricData>): String {
    // Placeholder logic for generating insights
    var fertilizerAdvice = "No fertilizer data"
    var waterAdvice = "No water data"
    var soilHealthAdvice = "Soil health data missing"

    metrics.forEach { metric ->
        // Simple logic based on certain metric names
        when (metric.name) {
            "Fertilizer Use" -> {
                fertilizerAdvice = "Weekly fertilizer usage is optimal"
            }
            "Water Usage" -> {
                waterAdvice = "Water usage is slightly high. Reduce watering"
            }
            "Soil Health" -> {
                soilHealthAdvice = "Soil health is good"
            }
        }
    }

    return "Fertilizer Advice: $fertilizerAdvice\nWater Usage: $waterAdvice\nSoil Health: $soilHealthAdvice"
}



@Preview(showBackground = true)
@Preview(showBackground = true)
@Composable
fun PreviewFarmAnalysisScreen() {
    val previewMetrics = listOf(
        MetricData(name = "Moisture", value = 45f, unit = "%"),
        MetricData(name = "Temperature", value = 23f, unit = "°C"),
        MetricData(name = "PH Level", value = 6.5f, unit = "")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        WeeklyTrendChart(metrics = previewMetrics)
        Spacer(modifier = Modifier.height(16.dp))
        InsightsSection(metrics = previewMetrics)
    }
}
