using AutoMapper;
using TemperatureMonitorModels;
using NodaTime;
using System;

namespace TemperatureMonitorApi
{
    public class ApiAutoMapperProfile: Profile
    {
        public ApiAutoMapperProfile()
        {
            CreateMap<TemperatureEntity, TemperatureViewModel>()
                .ForMember(vm => vm.Location, opt => opt.MapFrom(m => m.PartitionKey))
                .ForMember(vm => vm.Temperature, opt => opt.MapFrom(m => m.Temperature))
                .ForMember(vm => vm.Date, opt => opt.ResolveUsing(ConvertReverseTicksToLocalDateTimeString));

            CreateMap<TemperatureSummaryEntity, TemperatureSummaryViewModel>()
                .ForMember(vm => vm.Location, opt => opt.MapFrom(m => m.PartitionKey))
                .ForMember(vm => vm.Date, opt => opt.ResolveUsing(GetLocalDateFromContextOrRowKey));
        }

        private object GetLocalDateFromContextOrRowKey(TemperatureSummaryEntity source, TemperatureSummaryViewModel destination, string destMember, ResolutionContext context)
        {
            context.Items.TryGetValue("LocalDate", out var localDate);

            if (localDate is LocalDate) {
                return ((LocalDate)localDate).ToString("yyyy-MM-dd", null);
            }

            context.Items.TryGetValue("DateTimeZone", out var dateTimeZone);

            if (dateTimeZone is DateTimeZone && !string.IsNullOrWhiteSpace(source.RowKey))
            {
                return ReverseTicks.ToZonedDateTime(source.RowKey, (DateTimeZone)dateTimeZone).ToString("yyyy-MM-dd", null);
            }

            return null;
        }

        private object ConvertReverseTicksToLocalDateTimeString(TemperatureEntity temperature, TemperatureViewModel destination, string destMember, ResolutionContext context)
        {
            if (string.IsNullOrWhiteSpace(temperature.RowKey))
            {
                return null;
            }
            
            context.Items.TryGetValue("DateTimeZone", out var ianaTimeZone);

            var dateTimeZone = ianaTimeZone as DateTimeZone;
            
            if (dateTimeZone == null)
            {
                return null;
            }

            return ReverseTicks.ToZonedDateTime(temperature.RowKey, dateTimeZone).ToOffsetDateTime().ToString();
        }

        
    }
}
