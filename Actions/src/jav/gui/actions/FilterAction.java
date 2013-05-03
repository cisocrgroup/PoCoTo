///*
// * @author thorsten
// */
//package jav.gui.actions;
//
//import jav.gui.filter.ChainType;
//import jav.gui.filter.FilterChain;
//import jav.gui.filter.LenFilter;
//import jav.gui.filter.LevFilter;
//import jav.gui.main.MainController;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//public final class FilterAction implements ActionListener {
//
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        FilterChain filterChain = new FilterChain(ChainType.AND);
//        filterChain.addFilter(new LenFilter(">", 8, "Len>8"));
//        filterChain.addFilter(new LevFilter("<", 2, "Lev<2"));
//        filterChain.addFilter(new LevFilter(">", 0, "Lev>0"));
//
//        MainController.findInstance().applyFilter(filterChain);
//    }
//}
