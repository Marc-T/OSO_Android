package com.blackorwhite.osobackend;

        import java.io.IOException;
        import javax.servlet.ServletException;
        import javax.servlet.http.*;

public class CronController extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        MessagingEndpoint _Msg = new MessagingEndpoint();
        try {

            OSOClient Cli;
            Cli = new OSOClient();

            String State = Cli.downloadUrl("GetState");

            if (State.equals("2")){
                _Msg.sendMessage(OSOClient.alarm_salon_fired);
            }
            if (State.equals("3")){
                _Msg.sendMessage(OSOClient.alarm_garage_fired);
            }
            if (State.equals("5")){
                _Msg.sendMessage(OSOClient.alarmsilenced_salon_fired);
            }
            if (State.equals("6")){
                _Msg.sendMessage(OSOClient.alarmsilenced_garage_fired);
            }
        }
        catch (IOException ex) {
            _Msg.sendMessage(OSOClient.alarm_unreachable);
        }
    };
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}