/** Form definition */
case class Form(sections: List[Section])

/** Form section with a title and available options. */
case class Section(title: String, options: List[SectionOption] = Nil)

/** Option available in a form section */
case class SectionOption(label: String, suboptions: List[SubOption] = Nil)

/** Sub-option */
case class SubOption(label: String)
