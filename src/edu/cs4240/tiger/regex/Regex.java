package edu.cs4240.tiger.regex;

/**
 * @author Roi Atalla
 */
public abstract class Regex {
	public class Match {
		private String piece;
		
		public Match(String piece) {
			this.piece = piece;
		}
		
		public String getMatch() {
			return piece;
		}
		
		public void append(Match match) {
			this.piece += match.piece;
		}
	}
	
	private Regex() {}
	
	public abstract Match match(String input);
	
	public static Regex and(Regex ... regs) {
		return new Regex() {
			@Override
			public Match match(String input) {
				Match match = new Match("");
				
				for(Regex r : regs) {
					Match m = r.match(input.substring(match.piece.length()));
					if(m == null)
						return null;
					
					match.append(m);
				}
				
				return match;
			}
			
			@Override
			public String toString() {
				String s = "";
				for(Regex r : regs)
					s += r.toString();
				return s;
			}
		};
	}
	
	public static Regex or(Regex ... regs) {
		return new Regex() {
			@Override
			public Match match(String input) {
				for(Regex r : regs) {
					Match m = r.match(input);
					if(m == null)
						continue;
					
					return m;
				}
				
				return null;
			}
			
			@Override
			public String toString() {
				String s = "(";
				for(int i = 0; i < regs.length; i++) {
					s += regs[i].toString();
					if(i < regs.length - 1)
						s += "|";
				}
				return s + ")";
			}
		};
	}
	
	public static Regex optional(Regex regex) {
		return new Regex() {
			@Override
			public Match match(String input) {
				Match match = regex.match(input);
				if(match == null) {
					return new Match("");
				}
				return match;
			}
			
			@Override
			public String toString() {
				return regex.toString() + "?";
			}
		};
	}
	
	public static Regex zeroOrMore(Regex regex) {
		return new Regex() {
			@Override
			public Match match(String input) {
				Match match = new Match("");
				
				while(input.length() > match.piece.length()) {
					Match m = regex.match(input.substring(match.piece.length()));
					if(m == null) {
						return match;
					}
					
					match.append(m);
				}
				
				return match;
			}
			
			@Override
			public String toString() {
				return regex.toString() + "*";
			}
		};
	}
	
	public static Regex oneOrMore(Regex regex) {
		return new Regex() {
			@Override
			public Match match(String input) {
				Match match = null;
				
				while(match == null || input.length() > match.piece.length()) {
					Match m = regex.match(input.substring(match == null ? 0 : match.piece.length()));
					
					if(m == null) {
						return match;
					}
					
					if(match == null)
						match = new Match(m.piece);
					else
						match.append(m);
				}
				
				return match;
			}
			
			@Override
			public String toString() {
				return regex.toString() + "+";
			}
		};
	}
	
	public static Regex string(String s) {
		return new Regex() {
			@Override
			public Match match(String input) {
				if(input.startsWith(s)) {
					return new Match(input.substring(0, s.length()));
				}
				
				return null;
			}
			
			@Override
			public String toString() {
				String t = s;
				t = t.replace("+", "\\+");
				t = t.replace("*", "\\*");
				t = t.replace("?", "\\*");
				t = t.replace("(", "\\(");
				t = t.replace(")", "\\)");
				t = t.replace("|", "\\|");
				t = t.replace("[", "\\[");
				t = t.replace("]", "\\]");
				t = t.replace("!", "\\!");
				return t;
			}
		};
	}
	
	public static Regex letter() {
		return new Regex() {
			@Override
			public Match match(String input) {
				if(input.length() > 0 && Character.isAlphabetic(input.charAt(0)))
					return new Match(input.substring(0, 1));
				return null;
			}
			
			@Override
			public String toString() {
				return "[A-Za-z]";
			}
		};
	}
	
	public static Regex number() {
		return new Regex() {
			@Override
			public Match match(String input) {
				if(input.length() > 0 && Character.isDigit(input.charAt(0)))
					return new Match(input.substring(0, 1));
				return null;
			}
			
			@Override
			public String toString() {
				return "[0-9]";
			}
		};
	}
	
	public static Regex alphanumeric() {
		return new Regex() {
			@Override
			public Match match(String input) {
				if(input.length() > 0 && (Character.isAlphabetic(input.charAt(0)) || Character.isDigit(input.charAt(0))))
					return new Match(input.substring(0, 1));
				return null;
			}
			
			@Override
			public String toString() {
				return "[A-Za-z0-9]";
			}
		};
	}
	
	public static Regex wordChar() {
		return new Regex() {
			@Override
			public Match match(String input) {
				if(input.length() > 0 && (Character.isAlphabetic(input.charAt(0)) || Character.isDigit(input.charAt(0)) || input.charAt(0) == '_'))
					return new Match(input.substring(0, 1));
				return null;
			}
			
			@Override
			public String toString() {
				return "[A-Za-z0-9_]";
			}
		};
	}
	
	public static Regex whitespace() {
		return new Regex() {
			@Override
			public Match match(String input) {
				if(input.length() > 0 && Character.isWhitespace(input.charAt(0)))
					return new Match(input.substring(0, 1));
				return null;
			}
			
			@Override
			public String toString() {
				return "\\s";
			}
		};
	}
}
