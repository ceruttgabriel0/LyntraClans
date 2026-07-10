package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.storage.dao.NoticeDao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NoticeManager {

    private static final int MAX_NOTICES = 10;

    private final Logger logger;
    private final NoticeDao noticeDao;

    public NoticeManager(Logger logger, NoticeDao noticeDao) {
        this.logger = logger;
        this.noticeDao = noticeDao;
    }

    public void post(int clanId, UUID authorUuid, String message) {
        try {
            noticeDao.insert(clanId, authorUuid, message, System.currentTimeMillis());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao postar aviso", e);
        }
    }

    public List<NoticeDao.Notice> getNotices(int clanId) {
        try {
            return noticeDao.findByClan(clanId, MAX_NOTICES);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao consultar avisos", e);
            return List.of();
        }
    }

    public void removeAllForClan(int clanId) {
        try {
            noticeDao.deleteByClan(clanId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao remover avisos do cla", e);
        }
    }
}
