package domain;

import java.util.ArrayList;
import java.util.List;
//Observer هي واجهة Interface تحدد ما الذي يجب أن يفعله أي كائن مراقب (مثل البريد الإلكتروني أو SMS أو إشعارات الهاتف).
//
//فيها دالة واحدة اسمها notify:
//معناها: عندما نريد أن نرسل إشعارًا، نمرر المستخدم (User) والرسالة (message).

// Observer interface for notifications
 interface Observer {
    void notify(User user, String message);
}

//الشرح:
//
//هذا الكلاس يُنفذ الواجهة Observer، يعني هو نوع من أنواع المراقبين.
//
//هنا المراقب هو مرسل البريد الإلكتروني.

// EmailNotifier implementation of Observer
public class EmailNotifier implements Observer {
    private EmailServer emailServer;

    public EmailNotifier(EmailServer emailServer) {
        this.emailServer = emailServer;
    }

    @Override
    public void notify(User user, String message) {
        if (emailServer == null) {
            System.err.println("تحذير: خادم البريد الإلكتروني غير متاح. لم يتم إرسال الإشعار.");
            return;
        }
        if (user == null || user.getUsername() == null) {
            System.err.println("تحذير: بيانات المستخدم غير صحيحة. لم يتم إرسال الإشعار.");
            return;
        }
        try {
            String email = user.getUsername() + "@example.com"; // Assuming username is email
            emailServer.sendEmail(email, "part 3 Library Notification", message);
        } catch (Exception e) {
            System.err.println("فشل في إرسال البريد الإلكتروني: " + e.getMessage());
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
}

