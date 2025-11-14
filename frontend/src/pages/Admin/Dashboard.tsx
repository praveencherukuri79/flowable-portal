import React from 'react'
import {
  Box,
  Card,
  CardContent,
  Grid,
  Stack,
  Typography,
} from '@mui/material'
import { BarChart } from '@mui/x-charts/BarChart'
import { LineChart } from '@mui/x-charts/LineChart'
import { PieChart } from '@mui/x-charts/PieChart'
// Gauge chart removed - not available in current MUI X Charts version
// import { Gauge, gaugeClasses } from '@mui/x-charts/Gauge'
import { adminApi, Metrics } from '../../api/adminApi'

export default function Dashboard() {
  const [metrics, setMetrics] = React.useState<Metrics | null>(null)
  const [loading, setLoading] = React.useState(true)

  React.useEffect(() => {
    adminApi.getMetrics()
      .then((res) => setMetrics(res.data))
      .catch((err) => console.error('Failed to load metrics:', err))
      .finally(() => setLoading(false))
  }, [])

  const running = metrics?.runningInstances ?? 0
  const completed = metrics?.completedInstances ?? 0
  const tasks = metrics?.totalTasks ?? 0
  const totalInstances = running + completed
  const runningPct = totalInstances ? Math.round((running / totalInstances) * 100) : 0

  const days = metrics?.instancesByDay?.map((d) => d.day) ?? []
  const dayCounts = metrics?.instancesByDay?.map((d) => d.count) ?? []
  const taskStatePie =
    metrics?.tasksByState?.map((t, i) => ({
      id: i,
      label: t.state,
      value: t.count,
    })) ?? []
  const avgDur = metrics?.avgDurationByDefinition ?? []

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <Typography>Loading dashboard...</Typography>
      </Box>
    )
  }

  return (
    <Grid container spacing={2}>
      {/* KPI Cards */}
      <Grid item xs={12} md={3}>
        <Kpi label="Running Instances" value={running} />
      </Grid>
      <Grid item xs={12} md={3}>
        <Kpi label="Completed Instances" value={completed} />
      </Grid>
      <Grid item xs={12} md={3}>
        <Kpi label="Total Tasks" value={tasks} />
      </Grid>
      <Grid item xs={12} md={3}>
        <Card>
          <CardContent>
            <Stack spacing={2}>
              <Typography variant="overline">Running Ratio</Typography>
              <Typography variant="h3" sx={{ color: 'primary.main', fontWeight: 600 }}>
                {runningPct}%
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Running: {running} / Total: {totalInstances}
              </Typography>
            </Stack>
          </CardContent>
        </Card>
      </Grid>

      {/* Instances by Day (Bar) */}
      <Grid item xs={12} md={7}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Instances by Day
            </Typography>
            <Box sx={{ width: '100%', overflowX: 'auto' }}>
              {days.length > 0 ? (
                <BarChart
                  height={300}
                  xAxis={[{ scaleType: 'band', data: days, label: 'Day' }]}
                  series={[{ data: dayCounts, label: 'Instances' }]}
                  margin={{ left: 50, right: 10, top: 20, bottom: 40 }}
                />
              ) : (
                <Typography variant="body2" color="text.secondary" sx={{ py: 5, textAlign: 'center' }}>
                  No data available
                </Typography>
              )}
            </Box>
          </CardContent>
        </Card>
      </Grid>

      {/* Tasks by State (Pie) */}
      <Grid item xs={12} md={5}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Tasks by State
            </Typography>
            {taskStatePie.length > 0 ? (
              <PieChart
                height={300}
                series={[
                  {
                    innerRadius: 60,
                    outerRadius: 120,
                    paddingAngle: 2,
                    arcLabel: (item) => `${item.value}`,
                    data: taskStatePie,
                  },
                ]}
                slotProps={{
                  legend: {
                    direction: 'row',
                    position: { vertical: 'bottom', horizontal: 'middle' },
                  },
                }}
              />
            ) : (
              <Typography variant="body2" color="text.secondary" sx={{ py: 5, textAlign: 'center' }}>
                No data available
              </Typography>
            )}
          </CardContent>
        </Card>
      </Grid>

      {/* Instances Trend (Line) */}
      <Grid item xs={12} md={6}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Instances Trend
            </Typography>
            {days.length > 0 ? (
              <LineChart
                height={300}
                xAxis={[{ scaleType: 'point', data: days }]}
                series={[{ data: dayCounts, label: 'Instances' }]}
                margin={{ left: 50, right: 10, top: 20, bottom: 40 }}
              />
            ) : (
              <Typography variant="body2" color="text.secondary" sx={{ py: 5, textAlign: 'center' }}>
                No data available
              </Typography>
            )}
          </CardContent>
        </Card>
      </Grid>

      {/* Avg Duration by Definition (Bar) */}
      <Grid item xs={12} md={6}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Avg Duration by Definition (min)
            </Typography>
            <Box sx={{ width: '100%', overflowX: 'auto' }}>
              {avgDur.length > 0 ? (
                <BarChart
                  height={300}
                  xAxis={[
                    {
                      scaleType: 'band',
                      data: avgDur.map((d) => d.definitionKey),
                      label: 'Definition',
                    },
                  ]}
                  series={[
                    {
                      data: avgDur.map((d) =>
                        Number(d.minutes?.toFixed ? d.minutes.toFixed(2) : d.minutes)
                      ),
                      label: 'Minutes',
                    },
                  ]}
                  margin={{ left: 60, right: 10, top: 20, bottom: 60 }}
                />
              ) : (
                <Typography variant="body2" color="text.secondary" sx={{ py: 5, textAlign: 'center' }}>
                  No data available
                </Typography>
              )}
            </Box>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  )
}

function Kpi({ label, value }: { label: string; value: number }) {
  return (
    <Card>
      <CardContent>
        <Stack spacing={0.5}>
          <Typography variant="overline">{label}</Typography>
          <Typography variant="h4">{value}</Typography>
        </Stack>
      </CardContent>
    </Card>
  )
}

