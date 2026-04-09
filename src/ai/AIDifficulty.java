package ai;

public enum AIDifficulty {
    EASY   ("Dễ"),
    MEDIUM ("Vừa"),
    HARD   ("Khó");

    public final String label;
    AIDifficulty(String label) { this.label = label; }

    public BattleStrategy createStrategy(String name) {
        switch (this) {
            case EASY:   return new EasyAI(name);
            case MEDIUM: return new MediumAI(name);
            case HARD:   return new SmartAI(name);
            default:     return new SmartAI(name);
        }
    }
}