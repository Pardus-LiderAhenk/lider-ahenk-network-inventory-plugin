package tr.org.liderahenk.network.inventory.constants;

/**
 * In SWT wizard pages, when next button clicked
 * getNextPage method runs. But getNextPage method also runs
 * in the opening of page and after setPageComplete executed.
 * That's why its confusing and not practical. I use these types
 * to control this complexity. For example, if I would like to
 * start an installation in the opening of a page, I just 
 * set a NextPageEventType variable as CLICK_FROM_PREV_PAGE 
 * and checking it with a condition on the page that I will 
 * start the installation. The page which needs this kind of
 * control should implement ControlNextEvent interface.
 * 
 * @author Caner Feyzullahoğlu <caner.feyzullahoglu@agem.com.tr>
 */
public enum NextPageEventType {
	SET_PAGE_COMPLETE,
	NEXT_BUTTON_CLICK,
	CLICK_FROM_PREV_PAGE
}
