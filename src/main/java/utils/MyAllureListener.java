package utils;

import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import io.qameta.allure.junit4.AllureJunit4;
import org.junit.runner.notification.Failure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyAllureListener extends AllureJunit4 {

    @Override
    public void testFailure(Failure failure) {
        try {
            // Получаем текущий WebDriver
            byte[] screenshot = ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES);

            // Генерируем имя для скриншота
            String testName = failure.getDescription().getDisplayName();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String screenshotName = String.format("%s_%s.png", testName, timestamp);

            // Добавляем скриншот в отчет Allure
            Allure.getLifecycle().addAttachment(screenshotName, "image/png", null, screenshot);
        } catch (Exception e) {
            e.printStackTrace(); // Логгируем ошибки при снятии скриншота
        }

        super.testFailure(failure);
    }
}
