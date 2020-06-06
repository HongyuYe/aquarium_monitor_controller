using Emmellsoft.IoT.Rpi.SenseHat;
using Emmellsoft.IoT.Rpi.SenseHat.Fonts.SingleColor;
using System;
using System.Threading.Tasks;
using Windows.UI;

namespace TemperatureLoggerActions
{
    public class SenseHatTemperatureDisplay : ILoggerAction<double>
    {
        private readonly ISenseHat _senseHat;

        public SenseHatTemperatureDisplay(ISenseHat senseHat)
        {
            _senseHat = senseHat;
        }

        public async Task LogAction(double obj)
        {
            await Task.Run(() =>
            {
                var tinyFont = new TinyFont();

                ISenseHatDisplay display = _senseHat.Display;

                double temperatureValue = obj;

                int temperature = (int)Math.Round(temperatureValue);

                string text = temperature.ToString();

                if (text.Length > 2)
                {
                    // Too long to fit the display!
                    text = "**";
                }

                display.Clear();
                Color color;
                color = GetTextColor(temperature);

                tinyFont.Write(display, text, color);
                display.Update();
            });
        }

        private static Color GetTextColor(int temperature)
        {
            Color color;
            switch (temperature)
            {
                case int n when (n < 50):
                    color = Colors.Red;
                    break;

                case int n when (n >= 50 && n < 80):
                    color = Colors.Green;
                    break;

                default:
                    color = Colors.Blue;
                    break;
            }

            return color;
        }
    }
}
