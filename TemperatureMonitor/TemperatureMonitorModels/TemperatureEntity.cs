using Microsoft.WindowsAzure.Storage.Table;
using System;

namespace TemperatureMonitorModels
{
    public class TemperatureEntity: TableEntity
    {
        public TemperatureEntity()
        {

        }

        public TemperatureEntity(string location, int temperature)
        {
            PartitionKey = location;
            RowKey = DateTime.UtcNow.ToReverseTicks();
            Temperature = temperature;
        }

        public int Temperature { get; set; }
    }
}