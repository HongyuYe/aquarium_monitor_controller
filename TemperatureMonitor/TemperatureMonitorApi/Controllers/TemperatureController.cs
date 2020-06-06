using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.WindowsAzure.Storage;
using Microsoft.WindowsAzure.Storage.Table;
using TemperatureMonitorModels;
using NodaTime;
using AutoMapper;

namespace TemperatureMonitorApi.Controllers
{
    
    [Route("api/[controller]")]
    public class TemperatureController : Controller
    {
        private readonly CloudTableClient _client;
        private readonly CloudTable _table;
        private readonly string _tableName;

        public TemperatureController()
        {
            var storageAccount = CloudStorageAccount.Parse("DefaultEndpointsProtocol=https;AccountName=aquariumlogger;AccountKey=MKcarWLA8QU+1eXz13jP13PYE722SZk/MMIwX8F86qOSLNhuI5//N2E2SjUP0uNNlYGX2NzB0FwCDoJvYI86Pw==;EndpointSuffix=core.windows.net");
            _client = storageAccount.CreateCloudTableClient();
            _tableName = "TemperatureLog";
            _table = _client.GetTableReference(_tableName);
        }

        [HttpGet]
        public async Task<IActionResult> Get(string location, string ianaTimeZone)
        {
            var operation = new TableQuery<TemperatureEntity>()
                .Where(TableQuery.GenerateFilterCondition("PartitionKey", QueryComparisons.Equal, location))
                .Take(1);
            var temperature = (await _table.ExecuteQuerySegmentedAsync(operation, null)).FirstOrDefault();
            if (temperature != null)
            {
                var dateTimeZone = DateTimeZoneProviders.Tzdb.GetZoneOrNull(ianaTimeZone);

                if (dateTimeZone == null)
                {
                    return BadRequest($"Unknown IANA time zone: {ianaTimeZone}");
                }

                return Ok(Mapper.Map<TemperatureViewModel>(temperature, opts =>
                {
                    opts.Items["DateTimeZone"] = dateTimeZone;
                }));
            }
            else
            {
                return NotFound();
            }
        }


        [HttpGet]
        [Route("history")]
        public async Task<IActionResult> History(string location, string ianaTimeZone, DateTime date)
        {
            var dateTimeZone = DateTimeZoneProviders.Tzdb.GetZoneOrNull(ianaTimeZone);

            if (dateTimeZone == null)
            {
                return BadRequest($"Unknown IANA time zone: {ianaTimeZone}");
            }

            var zonedDateTime = LocalDateTime.FromDateTime(date).InZoneLeniently(dateTimeZone);
            var result = await GetTemperatureEntitiesForDate(location, zonedDateTime);

            return Ok(Mapper.Map<List<TemperatureViewModel>>(result, opts =>
            {
                opts.Items["DateTimeZone"] = dateTimeZone;
            }));
        }

        [HttpGet]
        [Route("summary")]
        public async Task<IActionResult> Summary(string location, DateTime startDate, DateTime endDate, string ianaTimeZone)
        {
            var dateTimeZone = DateTimeZoneProviders.Tzdb.GetZoneOrNull(ianaTimeZone);

            if (dateTimeZone == null)
            {
                return BadRequest($"Unknown IANA time zone: {ianaTimeZone}");
            }

            var localStartDate = new LocalDateTime(startDate.Year, startDate.Month, startDate.Day, 0, 0);
            var localEndDate = new LocalDateTime(endDate.Year, endDate.Month, endDate.Day, 0, 0);

            if (localStartDate > localEndDate)
            {
                return BadRequest("Given startDate must be less than or equal to given endDate");
            }

            var result = new List<TemperatureSummaryEntity>();

            for (var requestStartDate = localStartDate; requestStartDate <= localEndDate; requestStartDate = requestStartDate.PlusDays(1))
            {
                var temperatureRecords = await GetTemperatureEntitiesForDate(location, requestStartDate.InZoneLeniently(dateTimeZone));

                var temperatureSummaryEntity = new TemperatureSummaryEntity(location, requestStartDate, temperatureRecords);
                result.Add(temperatureSummaryEntity);
            }

            return base.Ok(Mapper.Map<List<TemperatureSummaryViewModel>>(result, opts =>
            {
                opts.Items["DateTimeZone"] = dateTimeZone;
            }));
        }

        public async Task<List<TemperatureEntity>> GetTemperatureEntitiesForDate(string location, ZonedDateTime zonedDateTime)
        {
            var dateTimeZone = zonedDateTime.Zone;
            var requestStartDateTime = zonedDateTime.ToDateTimeUtc();
            var requestEndDateTime = zonedDateTime.PlusHours(24).ToDateTimeUtc();

            var requestStartReverseTick = requestStartDateTime.ToReverseTicks();
            var requestEndReverseTick = requestEndDateTime.ToReverseTicks();

            //The nature of ReverseTicks is that the start time is greater than the end time.
            var operation = new TableQuery<TemperatureEntity>()
               .Where(TableQuery.CombineFilters(
                   TableQuery.GenerateFilterCondition("PartitionKey", QueryComparisons.Equal, location),
                   TableOperators.And,
                   TableQuery.CombineFilters(
                       TableQuery.GenerateFilterCondition("RowKey", QueryComparisons.LessThanOrEqual, requestStartReverseTick),
                       TableOperators.And,
                       TableQuery.GenerateFilterCondition("RowKey", QueryComparisons.GreaterThan, requestEndReverseTick))));

            return (await _table.ExecuteQuerySegmentedAsync(operation, null)).ToList();
        }


        [HttpGet]
        [Route("latest")]
        public async Task<IActionResult> SummaryOne(string location, string ianaTimeZone)
        {
 
            var result = new List<TemperatureEntity>();
            var temp = await GetTemperatureEntitiesForDateOne(location);
            result.Add(temp);

            return base.Ok(result);

        }



        public async Task<TemperatureEntity> GetTemperatureEntitiesForDateOne(string location)
        {
            var operation = new TableQuery<TemperatureEntity>()
               .Where(TableQuery.GenerateFilterCondition("PartitionKey", QueryComparisons.Equal, location));
 
            return (await _table.ExecuteQuerySegmentedAsync(operation, null)).OrderByDescending(o => o.Timestamp).First();
        }

    }
}
