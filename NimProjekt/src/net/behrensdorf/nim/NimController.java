package net.behrensdorf.nim;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

/**
 * Servlet implementation class NimController
 */
@WebServlet("/Game")
public class NimController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	class BagForNimView {
		String action;
		String message;
		int[] values;
		int[] move;
		boolean gameover;
		int player;
		int hashCode;

		BagForNimView(String controllerAction, boolean suggestions, Nim nim) {
			NimMove move;
			switch (controllerAction) {
			case "init":
				this.action = "init_nimtable";
				this.values = nim.getRows();
				this.player = 1;
				break;
			case "humanmove":
				this.action = "confirm_humanmove";
				move = nim.getLastMove();
				this.move = new int[] { move.row, move.count };
				;
				this.player = 2;
				break;

			case "computermove":
				this.action = "confirm_computermove";
				move = nim.getLastMove();
				this.move = new int[] { move.row, move.count };
				;
				this.player = 1;
				break;

			default:
				break;
			}
			this.message = getMessage(controllerAction, suggestions, nim);
			this.gameover = nim.isOver();
			this.hashCode = nim.hashCode();

		}
	};

	private static String getSuggestionMessage(Nim nim) {
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

	private static String getMessage(String action, boolean suggestions, Nim nim) {
		String message = "";
		switch (action) {
		case "init":
			message = NimMessages.getStartMessage();
			if (suggestions && nim.isWinSituation()) {
				message += "<br>" + "Zugvorschlag: " + getSuggestionMessage(nim);
			}
			break;
		case "humanmove":
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
			break;
		case "computermove":
			NimMove move = nim.getLastMove();
			int oldCount = nim.getOldValueOflastMove().count;
			int row = move.row;
			int count = move.count;
			int weg = oldCount - count;

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
					message += "<br>Jetzt bist du dran. Vorschlag: " + getSuggestionMessage(nim);

				} else {
					message += "<br>Jetzt bist du dran.";

				}

			}

			break;
		}
		return message;
	}

	public static String getComputerBehaviourText(int computerBehaviour) {
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

	public static String getSuggestionsText(boolean suggestions) {
		if (suggestions)
			return "ja";
		else
			return "nein";
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		RequestDispatcher dispatcher = request.getRequestDispatcher("nimView.jsp");
		HttpSession session = request.getSession();

		Integer rowCount = 6;
		Integer maxTokenCount;
		Integer computerBehaviour = 1;
		Boolean suggestions = false;

		NimMessages.readGoodMessages(getServletContext().getRealPath("/resources/good_messages.txt"));
		NimMessages.readBadMessages(getServletContext().getRealPath("/resources/bad_messages.txt"));
		NimMessages.readStartMessages(getServletContext().getRealPath("/resources/start_messages.txt"));

		String sTemp = request.getParameter("rowCount");

		try {
			rowCount = Integer.valueOf(sTemp);
		} catch (NumberFormatException e) {
			rowCount = 6;
		}

		session.setAttribute("rowCount", rowCount);

		sTemp = request.getParameter("tokenCount");
		try {
			maxTokenCount = Integer.valueOf(sTemp);
		} catch (NumberFormatException e) {
			maxTokenCount = 10;
		}
		session.setAttribute("maxTokenCount", maxTokenCount);

		sTemp = request.getParameter("computerBehaviour");
		try {
			computerBehaviour = Integer.valueOf(sTemp);
		} catch (NumberFormatException e) {
			computerBehaviour = 1;
		}
		session.setAttribute("computerBehaviour", computerBehaviour);

		sTemp = request.getParameter("suggestions");
		suggestions = false;
		if (sTemp != null && sTemp.trim().equalsIgnoreCase("yes")) {
			suggestions = true;
		}
		session.setAttribute("suggestions", suggestions);

		session.setAttribute("computerBehaviourText", getComputerBehaviourText(computerBehaviour));
		session.setAttribute("suggestionsText", getSuggestionsText(suggestions));
		dispatcher.forward(request, response);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action");
		HttpSession session = request.getSession();
		if (session.isNew()) {
			action = "start";
		}

		Integer row = -1;
		Integer count = -1;
		Nim nim;

		Integer rowCount = (Integer) session.getAttribute("rowCount");
		Integer maxTokenCount = (Integer) session.getAttribute("maxTokenCount");
		Boolean suggestions = (Boolean) session.getAttribute("suggestions");
		Integer computerBehaviour = (Integer) session.getAttribute("computerBehaviour");
		BagForNimView bag = null;
		Gson gson = new Gson();
		String json;
		if (action == null)
			action = "unknown";

		switch (action) {
		case "start":
			doGet(request, response);
			break;
		case "init":
			nim = new Nim(rowCount, maxTokenCount);
			session.setAttribute("nim", nim);
			bag = new BagForNimView(action, suggestions, nim);
			break;
		case "humanmove":
			nim = (Nim) session.getAttribute("nim");
			row = Integer.valueOf(request.getParameter("row"));
			count = Integer.valueOf(request.getParameter("count"));
			nim.doMove(new NimMove(row, count));
			bag = new BagForNimView(action, suggestions, nim);
			break;
		case "computermove":
			nim = (Nim) session.getAttribute("nim");
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

			nim.doMove(move);
			bag = new BagForNimView(action, suggestions, nim);
			break;

		default:
			response.getWriter().append("Unhandeled Action...");

		}
		// nim = (Nim) session.getAttribute("nim");
		// if (nim != null) {
		// int[] rows = nim.getRows();
		// for (int i=0;i<rows.length;i++) {
		// System.out.println(i+1 + "->" + rows[i]);
		// }
		// }
		if (bag != null) {
			json = gson.toJson(bag);
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			// System.out.println(json);
			response.getWriter().write(json);

		}
	}

}
