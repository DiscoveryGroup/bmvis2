package biomine.bmvis2.pipeline;

public interface SettingsChangeCallback {
	public void settingsChanged(boolean redoGroupings);
	
	/**
	 * Does nothing when called
	 */
	public static SettingsChangeCallback NOP = new SettingsChangeCallback() {
		public void settingsChanged(boolean redoGroupings) {
			//do nothing
		}
	};
}
