package com.github.k1rakishou.kurobanewnavstacktest.data

interface IAttachment

class AddNewFileButton : IAttachment
data class ReplyAttachmentFile(val id: Long, val color: Int) : IAttachment