using System;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using OpenQA.Selenium;
using OpenQA.Selenium.DevTools;
using OpenQA.Selenium.DevTools.V117.Performance;
using OpenQA.Selenium.DevTools.V117.Network;

namespace SeleniumDocs.Bidirectional
{
    [TestClass]
    public class CdpApiTest : BaseTest
    {
        [TestInitialize]
        public void Startup()
        {
            StartDriver();
        }        

        [TestMethod]
        public async Task SetCookie()
        {
            var devTools = driver as IDevTools;
            var session = devTools.GetDevToolsSession();
            
            var domains = session.GetVersionSpecificDomains<OpenQA.Selenium.DevTools.V117.DevToolsSessionDomains>();
            await domains.Network.Enable(new OpenQA.Selenium.DevTools.V117.Network.EnableCommandSettings());

            var cookieCommandSettings = new SetCookieCommandSettings
            {
                Name = "cheese",
                Value = "gouda",
                Domain = "www.selenium.dev",
                Secure = true
            };

            await domains.Network.SetCookie(cookieCommandSettings);

            driver.Url = "https://www.selenium.dev";
            OpenQA.Selenium.Cookie cheese = driver.Manage().Cookies.GetCookieNamed("cheese");
            Assert.AreEqual("gouda", cheese.Value);
        }

        [TestMethod]
        public async Task PerformanceMetrics()
        {
            driver.Url = "https://www.selenium.dev/selenium/web/frameset.html";

            IDevTools devTools = driver as IDevTools;
            DevToolsSession session = devTools.GetDevToolsSession();

            var domains = session.GetVersionSpecificDomains<OpenQA.Selenium.DevTools.V117.DevToolsSessionDomains>();
            await domains.Performance.Enable(new OpenQA.Selenium.DevTools.V117.Performance.EnableCommandSettings());

            var metricsResponse =
                await session.SendCommand<GetMetricsCommandSettings, GetMetricsCommandResponse>(
                    new GetMetricsCommandSettings()
                );

            var metrics = metricsResponse.Metrics.ToDictionary(
                dict => dict.Name,
                dict => dict.Value);

            Assert.IsTrue(metrics["DevToolsCommandDuration"] > 0);
            Assert.AreEqual(12, metrics["Frames"]);
        }

        [TestMethod]
        public async Task BasicAuth()
        {
            var devTools = driver as IDevTools;
            var session = devTools.GetDevToolsSession();
            var domains = session.GetVersionSpecificDomains<OpenQA.Selenium.DevTools.V117.DevToolsSessionDomains>();
            await domains.Network.Enable(new OpenQA.Selenium.DevTools.V117.Network.EnableCommandSettings());

            string authInfo = "admin:admin";
            string encodedAuth = Convert.ToBase64String(Encoding.Default.GetBytes(authInfo));

            SetExtraHTTPHeadersCommandSettings headerSettings = new SetExtraHTTPHeadersCommandSettings
            {
                Headers = new Headers() {
                    { "authorization", "Basic " + encodedAuth }
                }
            };
            await domains.Network.SetExtraHTTPHeaders(headerSettings);
            
            driver.Url = "https://the-internet.herokuapp.com/basic_auth";
        
            var element = driver.FindElement(By.TagName("p"));
            Assert.AreEqual("Congratulations! You must have the proper credentials.", element.Text);
        }
    }
}