using Microsoft.WindowsAzure.Storage.Table;
using NodaTime;
using System.Collections.Generic;
using System.Linq;

namespace TemperatureMonitorModels
{
    public class TemperatureSummaryEntity: TableEntity
    {
        public TemperatureSummaryEntity()
        {

        }

        public TemperatureSummaryEntity(string location, LocalDateTime date, List<TemperatureEntity> dataPoints)
        {
            PartitionKey = location;
            RowKey = date.ToDateTimeUnspecified().ToReverseTicks();

            if (dataPoints != null && dataPoints.Any())
            {
                Min = dataPoints.Min(he => he.Temperature);
                Max = dataPoints.Max(he => he.Temperature);
            }
        }

        public int? Min { get; set; }

        public int? Max { get; set; }

    }
}
