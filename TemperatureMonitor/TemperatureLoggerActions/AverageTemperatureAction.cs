using System.Collections.Concurrent;
using System.Linq;
using System.Threading.Tasks;

namespace TemperatureLoggerActions
{
    public class AverageTemperatureAction : ILoggerAction<double>
    {
        private const byte ReadingsBeforeAveraging = 2;
        private ConcurrentBag<double> _temperatureReadings;
        private readonly ILoggerAction<double>[] _childLoggerActions;

        public AverageTemperatureAction(params ILoggerAction<double>[] childLoggerActions)
        {
            _childLoggerActions = childLoggerActions;
            _temperatureReadings = new ConcurrentBag<double>();
        }

        public async Task LogAction(double obj)
        {
            _temperatureReadings.Add(obj);

            if (_temperatureReadings.Count >= 2)
            {
                var average = _temperatureReadings.Average();
                _temperatureReadings = new ConcurrentBag<double>();

                foreach (var action in _childLoggerActions)
                {
                    await action.LogAction(average);
                }
            }
        }
        
    }
}
