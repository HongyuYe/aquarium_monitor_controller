using System.Threading.Tasks;

namespace TemperatureLoggerActions
{
    public interface ILoggerAction<T>
    {
        /// <summary>
        /// Log operation on the given object
        /// </summary>
        /// <param name="obj">Object to log</param>
        Task LogAction(T obj);
    }
}
