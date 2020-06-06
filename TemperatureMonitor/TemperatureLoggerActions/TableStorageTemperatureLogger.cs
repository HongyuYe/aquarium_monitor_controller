using System;
using System.Threading.Tasks;
using Microsoft.WindowsAzure.Storage;
using Microsoft.WindowsAzure.Storage.Table;
using TemperatureMonitorModels;

namespace TemperatureLoggerActions
{
    public class TableStorageTemperatureLogger : ILoggerAction<double>
    {
        private CloudTableClient _client;
        private readonly string _location;
        private readonly string _tableName;
        private int _lastReading = -1;

        public TableStorageTemperatureLogger(string location, string tableName)
        {
            _location = location;
            _tableName = tableName;
        }

        public void initStorageConnection(string storageConnectionString)
        {
            var storageAccount = CloudStorageAccount.Parse(storageConnectionString);
            _client = storageAccount.CreateCloudTableClient();
        }

        public async Task LogAction(double obj)
        {
            var currentReading = Convert.ToInt32(obj);
            //if (currentReading == _lastReading)
            //{
            //    //Do nothing
            //    return;
            //}

            _lastReading = currentReading;
            var temperatureTable = _client.GetTableReference(_tableName);
            await temperatureTable.CreateIfNotExistsAsync();
            
            var entity = new TemperatureEntity(_location, currentReading);
            var operation = TableOperation.InsertOrReplace(entity);
            var result = await temperatureTable.ExecuteAsync(operation);
        }
    }
}
