package net.behrensdorf.nim;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class NimController
 */
@WebServlet("/Game")
public class NimController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Integer rowCount = 6;
	private Integer maxTokenCount;
	private Integer computerBehaviour = 1;
	private Boolean suggestions = false;
	private Nim nim;

	private String getSuggestionMessage() {
		String msg = "";
		NimMove sMove = nim.suggestMove();
		int sRow = sMove.row;
		int sCount = sMove.count;
		if (sCount == 0) {
			msg = "Mach die " + (sRow + 1) + ". Reihe leer.";
		} else {
			msg += "Lass in der " + (sRow + 1) + ". Reihe " + (sCount == 1 ? "einen Stein" : sCount + " Steine")
					+ " liegen.";
		}
		return msg;
	}

	public String getComputerBehaviour() {
		String result = "";
		switch (computerBehaviour) {
		case 1:
			result = "intelligent";
			break;
		case 2:
			result = "chaotisch";
			break;
		case 3:
			result = "dumm";
			break;
		}

		return result;
	}

	public String getSuggestionSetting() {
		if (suggestions)
			return "ja";
		else
			return "nein";
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("nimView.jsp");

		NimMessages.readGoodMessages(getServletContext().getRealPath("/resources/good_messages.txt"));
		NimMessages.readBadMessages(getServletContext().getRealPath("/resources/bad_messages.txt"));
		NimMessages.readStartMessages(getServletContext().getRealPath("/resources/start_messages.txt"));

		String sTemp = request.getParameter("rowCount");
		try {
			rowCount = Integer.valueOf(sTemp);
		} catch (NumberFormatException e) {
			rowCount = 6;
		}

		request.setAttribute("numRows", rowCount);

		sTemp = request.getParameter("tokenCount");
		try {
			maxTokenCount = Integer.valueOf(sTemp);
		} catch (NumberFormatException e) {
			maxTokenCount = 10;
		}
		request.setAttribute("maxTokenCount", maxTokenCount);

		sTemp = request.getParameter("computerBehaviour");
		try {
			computerBehaviour = Integer.valueOf(sTemp);
		} catch (NumberFormatException e) {
			computerBehaviour = 1;
		}

		sTemp = request.getParameter("suggestions");
		this.suggestions = false;
		if (sTemp != null && sTemp.trim().equalsIgnoreCase("yes")) {
			this.suggestions = true;
		}

		request.setAttribute("computerBehaviour", getComputerBehaviour());
		request.setAttribute("suggestionSetting", getSuggestionSetting());
		request.setAttribute("testMsg", NimMessages.getStartMessage());
		dispatcher.forward(request, response);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action");
		Integer row = -1;
		Integer count = -1;
		String message = "";
		if (action == null)
			action = "unknown";
		JsonWriter jsonWriter;
		switch (action) {
		case "start":
			doGet(request, response);
			break;
		case "init":
			nim = new Nim(rowCount, maxTokenCount);
			int[] rows = nim.getRows();
			message = NimMessages.getStartMessage();
			if (suggestions && nim.isWinSituation()) {
				message += "<br>" + "Zugvorschlag: " + getSuggestionMessage();
			}

			response.setCharacterEncoding("UTF-8");
			//@formatter:off
			JsonArrayBuilder arr = Json.createArrayBuilder();
			for (int i : rows)
				arr.add(i);
			try (StringWriter stWriter = new StringWriter()) {
				jsonWriter = Json.createWriter(stWriter);
				jsonWriter.writeObject(Json.createObjectBuilder()
					.add("action", "init_nimtable")
					.add("values", arr)
					.add("message", message).add("player", 1)
					.build());
				//@formatter:on
				jsonWriter.close();
				try (PrintWriter out = response.getWriter()) {
					out.println(stWriter.toString());
				}
			}
			break;
		case "humanmove":
			row = Integer.valueOf(request.getParameter("row"));
			count = Integer.valueOf(request.getParameter("count"));
			nim.doMove(new NimMove(row, count));
			if (nim.isOver()) {
				message = "Du hast den letzten Stein genommen und bist deshalb der Sieger.";
				message += "<br>HERZLICHEN GLÜCKWUNSCH!!!";
			} else {
				if (nim.isWinSituation()) {
					message = NimMessages.getBadMessage() + "<br>Nun ist der Computer wieder dran.";
				} else {
					message = NimMessages.getGoodMessage()
							+ "<br>Aber nun ist erstmal der Computer wieder dran.<br>(<span class=\"small\">Klick auf \"Computer zieht\"</span>)";
				}
			}
			;
			response.setCharacterEncoding("UTF-8");
			//@formatter:off
			try (StringWriter stWriter = new StringWriter()) {
				jsonWriter = Json.createWriter(stWriter);
				jsonWriter.writeObject(Json.createObjectBuilder()
					.add("action", "confirm_humanmove")
					.add("move", Json.createArrayBuilder()
							.add(row)
							.add(count))
					.add("message", message)
					.add("player", 2)
					.add("gameover", nim.isOver())
					.build());
				//@formatter:on
				jsonWriter.close();
				try (PrintWriter out = response.getWriter()) {
					out.println(stWriter.toString());
				}
			}

			break;
		case "computermove":
			NimMove move = null;
			switch (computerBehaviour) {
			case 1:
				move = nim.suggestMove();
				break;
			case 2:
				move = nim.randomMove();
				break;
			case 3:
				move = nim.suggestBadMove();
				break;
			default:
				move = nim.randomMove();
			}

			int oldCount = nim.getCount(move.row);
			row = move.row;
			count = move.count;
			int weg = oldCount - count;
			nim.doMove(move);
			message = "Der Computer nimmt aus der " + (row + 1) + ". Reihe "
					+ (weg == 1 ? "einen Stein" : weg + " Steine") + ".<br>";
			if (nim.isOver()) {
				message = "Der Computer hat gewonnen!";
				message += "<br>Pech für dich.";
			} else {
				switch (count) {
				case 0:
					message += "Die Reihe ist jetzt leer.";
					break;
				case 1:
					message += "Es bleibt ein Stein liegen.";
					break;
				default:
					message += "Es bleiben " + count + " Steine liegen.";
				}
				if (suggestions && nim.isWinSituation()) {
					message += "<br>Jetzt bist du dran. Vorschlag: " + getSuggestionMessage();

				} else {
					message += "<br>Jetzt bist du dran.";

				}

			}
			response.setCharacterEncoding("UTF-8");
			//@formatter:off
			try (StringWriter stWriter = new StringWriter()) {
				jsonWriter = Json.createWriter(stWriter);
				jsonWriter.writeObject(Json.createObjectBuilder()
					.add("action", "confirm_computermove")
					.add("move", Json.createArrayBuilder()
							.add(row)
							.add(count))
					.add("message", message)
					.add("player", 1)
					.add("gameover", nim.isOver())
					.build());
				//@formatter:on
				jsonWriter.close();
				try (PrintWriter out = response.getWriter()) {
					out.println(stWriter.toString());
				}
			}

		default:
			response.getWriter().append("Unhandeled Action...");

		}

	}

}
