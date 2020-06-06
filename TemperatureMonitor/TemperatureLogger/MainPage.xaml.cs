using Emmellsoft.IoT.Rpi.SenseHat;
using Emmellsoft.IoT.Rpi.SenseHat.Fonts.SingleColor;
using System.Threading.Tasks;
using Windows.UI.Xaml.Controls;
using System;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;
using System.Collections.ObjectModel;
using Windows.System.Threading;
using TemperatureLoggerActions;
using System.Collections.Generic;
using Windows.UI;

namespace TemperatureLogger
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page, ILoggerAction<double>
    {
        public readonly ObservableCollection<string> Logs = new ObservableCollection<string>();

        public MainPage()
        {
            this.InitializeComponent();
            this.LogList.ItemsSource = Logs;

            SetUpAndLaunchTimer();
        }

        private void state(string text, ISenseHat _senseHat)
        {
            var tinyFont = new TinyFont();

            ISenseHatDisplay display = _senseHat.Display;

            if (text.Length > 2)
            {
                // Too long to fit the display!
                text = "**";
            }

            display.Clear();

            tinyFont.Write(display, text, Colors.White);
            display.Update();
        }

        private void SetUpAndLaunchTimer()
        {
            Task.Run(async () =>
            {
                var senseHat = await SenseHatFactory.GetSenseHat().ConfigureAwait(false);

                await CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                    CoreDispatcherPriority.Normal,
                    () =>
                    {
                        if (senseHat == null)
                        {
                            this.state("0-", senseHat);
                            WaitingTextBlock.Text = "No Sense Hat found. Please restart the app to try again.";
                        }
                        else
                        {
                            this.state("-0", senseHat);

                            WaitingTextBlock.Visibility = Windows.UI.Xaml.Visibility.Collapsed;
                            LogList.Visibility = Windows.UI.Xaml.Visibility.Visible;
                        }
                    });

                this.state("01", senseHat);
                var actions = new List<ILoggerAction<double>>();

                this.state("02", senseHat);

                actions.Add(new SenseHatTemperatureDisplay(senseHat));
                this.state("03", senseHat);

                var table = new TableStorageTemperatureLogger("Aquarium", "TemperatureLog");
                this.state("04", senseHat);

                actions.Add(new AverageTemperatureAction(this, table));

                this.state("05", senseHat);
                table.initStorageConnection("DefaultEndpointsProtocol=https;AccountName=aquariumlogger;AccountKey=MKcarWLA8QU+1eXz13jP13PYE722SZk/MMIwX8F86qOSLNhuI5//N2E2SjUP0uNNlYGX2NzB0FwCDoJvYI86Pw==;EndpointSuffix=core.windows.net");

                this.state("06", senseHat);

                TimeSpan period = TimeSpan.FromSeconds(5);
                ThreadPoolTimer PeriodicTimer = ThreadPoolTimer.CreatePeriodicTimer(async (source) =>
                {
                    this.state("--", senseHat);
                    senseHat.Sensors.HumiditySensor.Update();

                    if (senseHat.Sensors.Temperature.HasValue)
                    {
                        double temperatureValue = senseHat.Sensors.Temperature.Value;

                        foreach (var action in actions)
                        {
                            await action.LogAction(temperatureValue).ConfigureAwait(false);
                        }
                    }
                }, period);
            }).ConfigureAwait(false);
        }

        public async Task LogAction(double value)
        {
            await CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                CoreDispatcherPriority.Normal,
                () =>
                {
                    Logs.Add($"{DateTime.UtcNow:yyyy-MM-ddThh:mm:ssZ} {value:0.0} oC");
                    LogList.SelectedIndex = LogList.Items.Count - 1;
                    LogList.ScrollIntoView(LogList.SelectedItem);
                });
        }
    }
}
